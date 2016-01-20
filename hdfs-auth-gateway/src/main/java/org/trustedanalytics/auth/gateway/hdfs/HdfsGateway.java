/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.trustedanalytics.auth.gateway.hdfs;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.AclEntry;
import org.apache.hadoop.fs.permission.AclEntryType;
import org.apache.hadoop.fs.permission.FsAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import org.trustedanalytics.auth.gateway.hdfs.config.ExternalConfiguration;
import org.trustedanalytics.auth.gateway.hdfs.kerberos.KerberosProperties;
import org.trustedanalytics.auth.gateway.hdfs.utils.PathCreator;
import org.trustedanalytics.auth.gateway.hdfs.utils.Qualifiers;
import org.trustedanalytics.auth.gateway.spi.Authorizable;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

@Profile(Qualifiers.HDFS)
@Configuration
public class HdfsGateway implements Authorizable {

  private static final String NAME = "hdfs";

  private static final String ADMIN_POSTFIX = "_admin";

  @Autowired
  private KerberosProperties kerberosProperties;

  @Autowired
  private ExternalConfiguration externalConfiguration;

  @Autowired
  private PathCreator pathCreator;

  @Autowired
  private HdfsClient hdfsClient;

  @Override
  public void addOrganization(String orgId) throws AuthorizableGatewayException {
    try {
      hdfsClient.createDirectory(pathCreator.createOrgPath(orgId), orgId.concat(ADMIN_POSTFIX),orgId,
          HdfsPermission.USER_ALL_GROUP_EXECUTE.getPermission());
      hdfsClient.createDirectory(pathCreator.createOrgUsersPath(orgId), orgId.concat(ADMIN_POSTFIX), orgId,
          HdfsPermission.USER_ALL_GROUP_EXECUTE.getPermission());
      hdfsClient.createDirectory(pathCreator.createOrgTmpPath(orgId), orgId.concat(ADMIN_POSTFIX), orgId,
          HdfsPermission.USER_ALL_GROUP_ALL.getPermission());
      hdfsClient.createDirectory(pathCreator.createOrgAppPath(orgId), orgId.concat(ADMIN_POSTFIX), orgId,
          HdfsPermission.USER_ALL_GROUP_EXECUTE.getPermission());
      hdfsClient.createDirectory(pathCreator.createOrgBrokerPath(orgId), orgId.concat(ADMIN_POSTFIX), orgId,
          HdfsPermission.USER_ALL_GROUP_EXECUTE.getPermission());
      hdfsClient.createDirectory(pathCreator.createBrokerMetadataPath(orgId), orgId.concat(ADMIN_POSTFIX), orgId,
          HdfsPermission.USER_ALL_GROUP_EXECUTE.getPermission());
      hdfsClient.createDirectory(pathCreator.createBrokerUserspacePath(orgId), orgId.concat(ADMIN_POSTFIX), orgId,
          HdfsPermission.USER_ALL_GROUP_EXECUTE.getPermission());

      List<AclEntry> acl_techExecute_hiveExecute = hdfsClient.getAcl(
          kerberosProperties.getTechnicalPrincipal(), FsAction.EXECUTE,
          externalConfiguration.getHiveUser(), FsAction.EXECUTE,
          AclEntryType.USER);
      List<AclEntry> acl_techAll_hiveExecute = hdfsClient.getAcl(
          kerberosProperties.getTechnicalPrincipal(), FsAction.ALL,
          externalConfiguration.getHiveUser(), FsAction.EXECUTE,
          AclEntryType.USER);
      List<AclEntry> acl_tech_all = hdfsClient.getAcl(
          kerberosProperties.getTechnicalPrincipal(), FsAction.ALL, AclEntryType.USER);
      hdfsClient.setACLForDirectory(pathCreator.createOrgPath(orgId), acl_techExecute_hiveExecute);
      hdfsClient.setACLForDirectory(pathCreator.createOrgBrokerPath(orgId), acl_techExecute_hiveExecute);
      hdfsClient.setACLForDirectory(pathCreator.createBrokerUserspacePath(orgId), acl_techAll_hiveExecute);
      hdfsClient.setACLForDirectory(pathCreator.createBrokerMetadataPath(orgId), acl_tech_all);
    } catch (IOException e) {
      throw new AuthorizableGatewayException(String.format("Can't add organization: %s", orgId), e);
    }
  }

  @Override
  public void removeOrganization(String orgId) throws AuthorizableGatewayException {
    try {
      hdfsClient.deleteDirectory(pathCreator.createOrgPath(orgId));
    } catch (IOException e) {
      throw new AuthorizableGatewayException(String.format("Can't remove organization: %s", orgId),
          e);
    }
  }

  @Override
  public void addUserToOrg(String userId, String orgId) throws AuthorizableGatewayException {
    try {
      hdfsClient.createDirectory(pathCreator.createUserPath(orgId, userId), userId, orgId,
          HdfsPermission.USER_ALL.getPermission());
    } catch (IOException e) {
      throw new AuthorizableGatewayException(String.format("Can't add user: %s", userId), e);
    }
  }

  @Override
  public void removeUserFromOrg(String userId, String orgId) throws AuthorizableGatewayException {
    try {
      hdfsClient.deleteDirectory(pathCreator.createUserPath(orgId, userId));
    } catch (IOException e) {
      throw new AuthorizableGatewayException(String.format("Can't remove user: %s from org: %s",
          userId, orgId), e);
    }
  }

  @Override
  public void addUser(String userId) throws AuthorizableGatewayException {}

  @Override
  public void removeUser(String userId) throws AuthorizableGatewayException {}

  @Override
  public String getName() {
    return NAME;
  }

}
