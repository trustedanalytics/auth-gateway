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

package org.trustedanalytics.auth.gateway.hbase.integration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.protobuf.generated.AccessControlProtos;
import org.apache.hadoop.hbase.security.access.AccessControlLists;
import org.apache.hadoop.hbase.security.access.UserPermission;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.trustedanalytics.auth.gateway.hbase.HBaseGateway;
import org.trustedanalytics.auth.gateway.hbase.TestIntegrationApplication;
import org.trustedanalytics.auth.gateway.hbase.integration.config.HBaseTestConfiguration;
import org.trustedanalytics.auth.gateway.hbase.integration.config.HBaseTestingUtilityConfiguration;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.ServiceException;

@RunWith(SpringJUnit4ClassRunner.class)
@IntegrationTest("server.port=0")
@ActiveProfiles({"test", "hbase-auth-gateway"})
@SpringApplicationConfiguration(classes = {TestIntegrationApplication.class,
    HBaseTestConfiguration.class, HBaseTestingUtilityConfiguration.class})
public class HBaseGatewayIntegrationTest {

  @Autowired
  private Connection connection;

  @Autowired
  private HBaseGateway hBaseGateway;

  private static final String TEST_ORG_ID = "test-id";

  private static final String TEST_NAMESPACE_NAME = "testid";

  private static final String TEST_GROUP_NAME = "@test-id";

  private static final String TEST_TABLE_NAME = "test_table";

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void createOrg_createNamespace_namespaceExistWithPermissions()
      throws IOException, AuthorizableGatewayException, ServiceException {
    hBaseGateway.addOrganization(TEST_ORG_ID);

    checkIfNamespaceExistsWithACL(TEST_NAMESPACE_NAME, TEST_GROUP_NAME);
  }

  @Test
  public void createSecondOrgWithSameName_namespaceAlreadyExists_doNothing()
      throws IOException, AuthorizableGatewayException, ServiceException {
    hBaseGateway.addOrganization(TEST_ORG_ID);
    hBaseGateway.addOrganization(TEST_ORG_ID);

    checkIfNamespaceExistsWithACL(TEST_NAMESPACE_NAME, TEST_GROUP_NAME);
  }

  @Test
  public void deleteOrg_EmptyNamespace_deleteNamespace()
      throws AuthorizableGatewayException, IOException, ServiceException {
    hBaseGateway.addOrganization(TEST_ORG_ID);
    checkIfNamespaceExistsWithACL(TEST_NAMESPACE_NAME, TEST_GROUP_NAME);
    hBaseGateway.removeOrganization(TEST_ORG_ID);

    exception.expect(NamespaceNotFoundException.class);
    checkIfNamespaceExistsWithACL(TEST_NAMESPACE_NAME, TEST_GROUP_NAME);
  }

  @Test
  public void deleteOrg_NamespaceWithTable_deleteNamespace()
      throws AuthorizableGatewayException, IOException, ServiceException {
    hBaseGateway.addOrganization(TEST_ORG_ID);
    checkIfNamespaceExistsWithACL(TEST_NAMESPACE_NAME, TEST_GROUP_NAME);

    TableName tableName = TableName.valueOf(TEST_NAMESPACE_NAME + ":" + TEST_TABLE_NAME);
    HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);
    connection.getAdmin().createTable(tableDescriptor);

    hBaseGateway.removeOrganization(TEST_ORG_ID);

    exception.expect(NamespaceNotFoundException.class);
    checkIfNamespaceExistsWithACL(TEST_NAMESPACE_NAME, TEST_GROUP_NAME);
  }

  @Test
  public void deleteNotExistingOrg_doNothing()
      throws IOException, ServiceException, AuthorizableGatewayException {
    hBaseGateway.removeOrganization(TEST_ORG_ID);

    exception.expect(NamespaceNotFoundException.class);
    checkIfNamespaceExistsWithACL(TEST_NAMESPACE_NAME, TEST_GROUP_NAME);
  }

  private void checkIfNamespaceExistsWithACL(String namespaceName, String groupName)
      throws IOException, ServiceException {
    NamespaceDescriptor descriptor = connection.getAdmin().getNamespaceDescriptor(namespaceName);
    assertThat(descriptor.getName(), equalTo(namespaceName));

    List<UserPermission> permissions;
    try (Table acl = connection.getTable(AccessControlLists.ACL_TABLE_NAME)) {
      BlockingRpcChannel service = acl.coprocessorService(HConstants.EMPTY_START_ROW);
      AccessControlProtos.AccessControlService.BlockingInterface protocol =
          AccessControlProtos.AccessControlService.newBlockingStub(service);
      permissions = ProtobufUtil.getUserPermissions(protocol, namespaceName.getBytes());
    }

    assertThat(permissions.size(), equalTo(1));
    assertThat(permissions.get(0).getUser(), equalTo(groupName.getBytes()));
    assertThat(permissions.get(0).getActions().length, equalTo(1));
    assertThat(permissions.get(0).getActions()[0].name(), equalTo("CREATE"));
  }

}
