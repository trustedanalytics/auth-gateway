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

package org.trustedanalytics.auth.gateway.zookeeper.integration;

import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.auth.gateway.spi.Authorizable;
import org.trustedanalytics.auth.gateway.zookeeper.ZookeeperGateway;
import org.trustedanalytics.auth.gateway.zookeeper.client.ZookeeperClient;
import org.trustedanalytics.auth.gateway.zookeeper.client.ZookeeperPermission;
import org.trustedanalytics.auth.gateway.zookeeper.integration.zkoperations.ZookeeperTestOperations;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public abstract class IntegrationTestBase {

    private static final String USERNAME = "username";

  private String rootNode;
  private ZookeeperTestOperations zkTestOperations;
  private Authorizable sut;

  @Rule
  public TestName testName = new TestName();

  @Before
  public void setUp() throws Exception {
    // different zookeeper root node for each test - to avoid test server restart between tests
    rootNode = "/" + getClass().getSimpleName() + "_" + testName.getMethodName();
    zkTestOperations = getZkTestOperations();
    zkTestOperations.createNode(rootNode);

    sut = new ZookeeperGateway(getZookeeperClient(rootNode), USERNAME);
  }

  protected abstract ZookeeperTestOperations getZkTestOperations();

  protected abstract String getZkTestServerConnectionString();

  protected abstract ZookeeperClient getZookeeperClient(String rootNode);

  @After
  public void tearDown() throws Exception {
    zkTestOperations.close();
  }

  @Test
  public void addOrg_krbAndZookeeperWorks_krbCalledAndZnodeCreatedWithAcl() throws Exception {

    // arrange
    String orgId = "8de9c508-3d56-4dd8-ad99-5a22fe91333f";

    // act
    sut.addOrganization(orgId);

    // assert
    zkTestOperations.assertNodeExists(rootNode + "/" + orgId,
        withAcl(ZookeeperPermission.CRDWA, USERNAME));
  }

  @Test
  public void idempotence_addOrgManyTimes_shouldAddOnceAndTerminateWithoutErrors()
      throws Exception {

    // arrange
    String orgId = "8de9c508-3d56-4dd8-ad99-5a22fe91333f";

    // act
    sut.addOrganization(orgId);
    sut.addOrganization(orgId);
    sut.addOrganization(orgId);

    // assert
    zkTestOperations.assertNodeExists(rootNode + "/" + orgId,
        withAcl(ZookeeperPermission.CRDWA, USERNAME));
  }

  @Test
  public void addOrgAddOneUserToOrg_krbAndZookeeperWorks_krbCalledAndZnodeCreatedWithAcls()
      throws Exception {

    // arrange
    String userId = "user1";
    String orgId = "a844d33e-0ec2-401e-8a53-88131b92d3d7";
    sut.addOrganization(orgId);

    // act
    sut.addUserToOrg(userId, orgId);

    // assert
    zkTestOperations.assertNodeExists(rootNode + "/" + orgId, withAcls(ZookeeperPermission.CRDWA,
            USERNAME, ZookeeperPermission.CRDW, userId));
  }

  @Test
  public void idempotence_addOrgAndOneUserToOrgManyTimes_shouldAddOrgAndUserOnceAndTerminateWithoutErrors()
      throws Exception {

    // arrange
    String userId = "user1";
    String orgId = "a844d33e-0ec2-401e-8a53-88131b92d3d7";
    sut.addOrganization(orgId);

    // act
    sut.addUserToOrg(userId, orgId);
    sut.addUserToOrg(userId, orgId);
    sut.addUserToOrg(userId, orgId);

    // assert
    zkTestOperations.assertNodeExists(rootNode + "/" + orgId, withAcls(ZookeeperPermission.CRDWA,
            USERNAME, ZookeeperPermission.CRDW, userId));
  }

  @Test
  public void addOrgAddManyUsersToOrg_krbAndZookeeperWorks_krbCalledAndZnodeCreatedWithAcls()
      throws Exception {

    // arrange
    String userId1 = "user1";
    String userId2 = "user2";
    String orgId = "a8d743e3-0ca5-419a-a0f2-668adbe53d80";
    sut.addOrganization(orgId);

    // act
    sut.addUserToOrg(userId1, orgId);
    sut.addUserToOrg(userId2, orgId);

    // assert
    zkTestOperations.assertNodeExists(rootNode + "/" + orgId,
        withAcls(ZookeeperPermission.CRDWA, USERNAME, ZookeeperPermission.CRDW,
            userId1, ZookeeperPermission.CRDW, userId2));
  }

  @Test
  public void addOrgAddManyUsersToOrgAndDeleteOneOfThem_krbAndZookeeperWorks_krbCalledAndZnodeCreatedWithAcls()
      throws Exception {

    // arrange
    String userId1 = "user1";
    String userId2 = "user2";
    String orgId = "b3ccab31-57ed-4831-ad1f-cf1d7389da26";
    sut.addOrganization(orgId);
    sut.addUserToOrg(userId1, orgId);
    sut.addUserToOrg(userId2, orgId);

    // act
    sut.removeUserFromOrg(userId1, orgId);

    // assert
    zkTestOperations.assertNodeExists(rootNode + "/" + orgId, withAcls(ZookeeperPermission.CRDWA,
            USERNAME, ZookeeperPermission.CRDW, userId2));
  }

  @Test
  public void idempotence_addOrgAndOneUserToOrgAndDeleteUserManyTimes_shouldCreateNodeWithCorrectAclsAndTerminateWithoutErrors()
      throws Exception {

    // arrange
    String userId1 = "user1";
    String userId2 = "user2";
    String orgId = "b3ccab31-57ed-4831-ad1f-cf1d7389da26";
    sut.addOrganization(orgId);
    sut.addUserToOrg(userId1, orgId);
    sut.addUserToOrg(userId2, orgId);

    // act
    sut.removeUserFromOrg(userId1, orgId);
    sut.removeUserFromOrg(userId1, orgId);
    sut.removeUserFromOrg(userId1, orgId);

    // assert
    zkTestOperations.assertNodeExists(rootNode + "/" + orgId, withAcls(ZookeeperPermission.CRDWA,
            USERNAME, ZookeeperPermission.CRDW, userId2));
  }

  @Test
  public void addOrgRemoveOrg_krbAndZookeeperWorks_krbCalledAndZnodeNotExist() throws Exception {

    // arrange
    String orgId = "60329ebc-345d-4a03-abe1-5a50f1de182e";
    sut.addOrganization(orgId);

    // act
    sut.removeOrganization(orgId);

    // assert
    zkTestOperations.assertNodeNotExist(rootNode + "/" + orgId);
  }

  @Test
  public void idempotence_addOrgAndRemoveOrgManyTimes_shouldDeleteNodeAndTerminateWithoutErrors()
      throws Exception {

    // arrange
    String orgId = "60329ebc-345d-4a03-abe1-5a50f1de182e";
    sut.addOrganization(orgId);

    // act
    sut.removeOrganization(orgId);
    sut.removeOrganization(orgId);
    sut.removeOrganization(orgId);

    // assert
    zkTestOperations.assertNodeNotExist(rootNode + "/" + orgId);
  }

  private List<ACL> withAcl(ZookeeperPermission permissions, String user) {
    return Arrays.asList(new ACL(permissions.getPerms(), new Id("sasl", user)));
  }

  private List<ACL> withAcls(ZookeeperPermission permissionsUser1, String user1,
      ZookeeperPermission permissionsUser2, String user2) {

    return Arrays.asList(new ACL(permissionsUser1.getPerms(), new Id("sasl", user1)),
        new ACL(permissionsUser2.getPerms(), new Id("sasl", user2)));
  }

  private List<ACL> withAcls(ZookeeperPermission permissionsUser1, String user1,
      ZookeeperPermission permissionsUser2, String user2, ZookeeperPermission permissionsUser3,
      String user3) {

    return Arrays.asList(new ACL(permissionsUser1.getPerms(), new Id("sasl", user1)),
        new ACL(permissionsUser2.getPerms(), new Id("sasl", user2)),
        new ACL(permissionsUser3.getPerms(), new Id("sasl", user3)));
  }
}
