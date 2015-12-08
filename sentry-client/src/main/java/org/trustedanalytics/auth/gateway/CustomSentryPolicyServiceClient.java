/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.auth.gateway;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import org.apache.hadoop.security.SaslRpcServer;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.sentry.SentryUserException;
import org.apache.sentry.provider.db.SentryAlreadyExistsException;
import org.apache.sentry.provider.db.service.thrift.SentryPolicyService;
import org.apache.sentry.provider.db.service.thrift.SentryPolicyServiceClient;
import org.apache.sentry.provider.db.service.thrift.SentryPolicyStoreProcessor;
import org.apache.sentry.provider.db.service.thrift.TAlterSentryRoleAddGroupsRequest;
import org.apache.sentry.provider.db.service.thrift.TAlterSentryRoleAddGroupsResponse;
import org.apache.sentry.provider.db.service.thrift.TCreateSentryRoleRequest;
import org.apache.sentry.provider.db.service.thrift.TCreateSentryRoleResponse;
import org.apache.sentry.provider.db.service.thrift.TDropSentryRoleRequest;
import org.apache.sentry.provider.db.service.thrift.TDropSentryRoleResponse;
import org.apache.sentry.provider.db.service.thrift.TSentryGroup;
import org.apache.sentry.service.thrift.ServiceConstants;
import sentry.org.apache.thrift.protocol.TBinaryProtocol;
import sentry.org.apache.thrift.protocol.TMultiplexedProtocol;
import sentry.org.apache.thrift.protocol.TProtocol;
import sentry.org.apache.thrift.transport.TTransport;
import sentry.org.apache.thrift.transport.TTransportException;
import sentry.org.apache.thrift.transport.TSocket;

import org.apache.sentry.service.thrift.Status;
import sentry.org.apache.thrift.TException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

import java.io.IOException;

public class CustomSentryPolicyServiceClient {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(CustomSentryPolicyServiceClient.class);

  public static final String HOST_PLACEHOLDER = "/_HOST@";

  public static final String ADMIN_ORG_GROUP_POSTFIX = "_admin";

  private SentryPolicyService.Client client;

  private String address;
  private int port;
  private String principal;
  private String realm;
  private String superUser;
  private  TTransport transport;
  private UserGroupInformation ugi;

  public CustomSentryPolicyServiceClient(Builder builder) throws IOException {
    this.address = builder.getAddress();
    this.port = builder.getPort();
    this.principal = builder.getPrincipal();
    this.realm = builder.getRealm();
    this.ugi = builder.getUgi();
    this.superUser = builder.getSuperUser();
  }

  public void initialize() throws IOException {
    Preconditions.checkState(client == null && transport == null,
                             "SentryPolicyServiceClient already initialized!");

    // Resolve server host in the same way as they are doing on server side
    String sentryPrincipalPattern = principal + HOST_PLACEHOLDER + realm;
    String serverPrincipal = SecurityUtil.getServerPrincipal(sentryPrincipalPattern, address);
    String[] serverPrincipalParts = SaslRpcServer.splitKerberosName(serverPrincipal);
    transport = new SaslClientTransport(
        serverPrincipalParts[
            Preconditions.
                checkElementIndex(0, 3, "Not found principal name in sentry service principal"
                                        + serverPrincipal
                )],
        serverPrincipalParts[
            Preconditions.
                checkElementIndex(1,3, "Not found host in sentry service principal"
                                       + serverPrincipal)
            ]).withUGI(this.ugi);
    try {
      transport.open();
    } catch (TTransportException e) {
      throw new IOException("Transport exception while opening transport: " + e.getMessage(), e);
    }
    TProtocol tProtocol = new TBinaryProtocol(transport);

    TMultiplexedProtocol protocol =
        new TMultiplexedProtocol(tProtocol, SentryPolicyStoreProcessor.SENTRY_POLICY_SERVICE_NAME);
    client = new SentryPolicyService.Client(protocol);
  }

  public void destroy() {
    if (transport != null && transport.isOpen()) {
      transport.close();
    }
  }

  public synchronized void createRole(String roleName) throws AuthorizableGatewayException {
    TCreateSentryRoleRequest request = new TCreateSentryRoleRequest();
    request.setProtocol_version(ServiceConstants.ThriftConstants.TSENTRY_SERVICE_VERSION_CURRENT);
    request.setRequestorUserName(superUser);
    request.setRoleName(roleName);
    try {
      TCreateSentryRoleResponse response = client.create_sentry_role(request);
      Status.throwIfNotOk(response.getStatus());
    } catch (SentryAlreadyExistsException ignore) {
      LOGGER.info("Role " + roleName + " already exist. Ignore create role request.", ignore);
    } catch (TException | SentryUserException e) {
      throw new AuthorizableGatewayException("Can't create role: ", e);
    }
  }

  public synchronized void grantRoleToGroup(String groupName, String roleName)
      throws AuthorizableGatewayException {
    TAlterSentryRoleAddGroupsRequest request =
        new TAlterSentryRoleAddGroupsRequest(
            ServiceConstants.ThriftConstants.TSENTRY_SERVICE_VERSION_CURRENT,
            superUser,
            roleName,
            Sets.newHashSet(new TSentryGroup(groupName),
                            new TSentryGroup(groupName.concat(ADMIN_ORG_GROUP_POSTFIX))));
    try {
      TAlterSentryRoleAddGroupsResponse response = client.alter_sentry_role_add_groups(request);
      Status.throwIfNotOk(response.getStatus());
    } catch (TException | SentryUserException e) {
      throw new AuthorizableGatewayException("Can't add group to role: ", e);
    }
  }

  public void dropRoleIfExists(String roleName)
      throws AuthorizableGatewayException {
    TDropSentryRoleRequest request = new TDropSentryRoleRequest();
    request.setProtocol_version(ServiceConstants.ThriftConstants.TSENTRY_SERVICE_VERSION_CURRENT);
    request.setRequestorUserName(superUser);
    request.setRoleName(roleName);
    try {
      TDropSentryRoleResponse response = client.drop_sentry_role(request);
      Status status = Status.fromCode(response.getStatus().getValue());
      if (status == Status.NO_SUCH_OBJECT) {
        return;
      }
      Status.throwIfNotOk(response.getStatus());
    } catch (TException | SentryUserException e) {
      throw new AuthorizableGatewayException("Can't drop role: ", e);
    }
  }

  static class Builder {

    private String address;
    private int port;
    private String principal;
    private String realm;
    private UserGroupInformation ugi;
    private String superUser;
    public String getSuperUser() {
      return superUser;
    }

    public Builder superUser(String superUser) {
      this.superUser = superUser;
      return this;
    }

    Builder(UserGroupInformation ugi) {
      this.ugi = ugi;
    }

    public Builder address(String address) {
      this.address = address;
      return this;
    }

    public Builder port(int port) {
      this.port = port;
      return this;
    }

    public Builder principal(String principal) {
      this.principal = principal;
      return this;
    }

    public Builder realm(String realm) {
      this.realm = realm;
      return this;
    }

    String getAddress() {
      return address;
    }

    int getPort() {
      return port;
    }

    String getPrincipal() {
      return principal;
    }

    String getRealm() {
      return realm;
    }

    UserGroupInformation getUgi() {
      return ugi;
    }

    CustomSentryPolicyServiceClient build() throws IOException {
      return new CustomSentryPolicyServiceClient(this);
    }

  }

  private class SaslClientTransport extends SentryPolicyServiceClient.UgiSaslClientTransport {
    SaslClientTransport(String protocol, String serverName)
        throws IOException {
      super(SaslRpcServer.AuthMethod.KERBEROS.getMechanismName(),
            null,
            protocol,
            serverName,
            ServiceConstants.ClientConfig.SASL_PROPERTIES,
            null,
            new TSocket(address, port, ServiceConstants.ClientConfig.SERVER_RPC_CONN_TIMEOUT_DEFAULT),
            false);
    }

    SaslClientTransport withUGI(UserGroupInformation userGroupInformation) {
      ugi = userGroupInformation;
      return this;
    }
  }
}