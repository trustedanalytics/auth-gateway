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

package org.trustedanalytics.auth.gateway.zookeeper;

import org.apache.zookeeper.KeeperException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;
import org.trustedanalytics.auth.gateway.zookeeper.client.ZookeeperClient;
import org.trustedanalytics.auth.gateway.zookeeper.client.ZookeeperPermission;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ZookeeperAuthorizationTest {

  private static final String ADMIN_USER = "adminUsername";
  private static final String USER = "user";
  private static final String ORG_ID = "aa549477-2899-4889-b8a2-04930909ad67";
  private static final String ORG_NAME = "org";

  private ZookeeperGateway zookeeperGateway;

  @Mock
  private ZookeeperClient zkClient;

  @Before
  public void setUp() throws Exception {
    zookeeperGateway = new ZookeeperGateway(zkClient, ADMIN_USER);
  }

  @Test
  public void addOrganization_zkWorks_znodeAddedWithAdminAcl() throws Exception {
    zookeeperGateway.addOrganization(ORG_ID);
    verify(zkClient).createZnode(ORG_ID, ADMIN_USER, ZookeeperPermission.CRDWA);
  }

  @Test(expected = AuthorizableGatewayException.class)
  public void addOrganization_zkFails_exceptionRethrown() throws Exception {
    doThrow(new Exception()).when(zkClient).createZnode(ORG_ID, ADMIN_USER,
        ZookeeperPermission.CRDWA);
    zookeeperGateway.addOrganization(ORG_ID);
    verify(zkClient).createZnode(ORG_ID, ADMIN_USER, ZookeeperPermission.CRDWA);
  }

  @Test
  public void addUserToOrg_orgExists_aclAdded() throws Exception {
    zookeeperGateway.addUserToOrg(USER, ORG_ID);
    verify(zkClient).addUserToAcl(ORG_ID, USER, ZookeeperPermission.CRDW);
  }

  @Test(expected = AuthorizableGatewayException.class)
  public void addUserToOrg_orgNotExist_exceptionRethrown() throws Exception {
    doThrow(new KeeperException.NoNodeException()).when(zkClient).addUserToAcl(ORG_ID, USER,
        ZookeeperPermission.CRDW);
    zookeeperGateway.addUserToOrg(USER, ORG_ID);
    verify(zkClient).addUserToAcl(ORG_ID, USER, ZookeeperPermission.CRDW);
  }

  @Test(expected = AuthorizableGatewayException.class)
  public void addUserToOrg_zkFails_exceptionRethrown() throws Exception {
    doThrow(new Exception()).when(zkClient).addUserToAcl(ORG_ID, USER, ZookeeperPermission.CRDW);
    zookeeperGateway.addUserToOrg(USER, ORG_ID);
    verify(zkClient).addUserToAcl(ORG_ID, USER, ZookeeperPermission.CRDW);
  }

  @Test
  public void removeOrganization_orgExists_loggedInKrbThenAclRevoked() throws Exception {
    zookeeperGateway.removeOrganization(ORG_ID);
    verify(zkClient).deleteZnode(ORG_ID);
  }

  @Test(expected = AuthorizableGatewayException.class)
  public void removeOrganization_orgNotExist_exceptionRethrown() throws Exception {
    doThrow(new KeeperException.NoNodeException()).when(zkClient).deleteZnode(ORG_ID);
    zookeeperGateway.removeOrganization(ORG_ID);
    verify(zkClient).deleteZnode(ORG_ID);
  }

  @Test(expected = AuthorizableGatewayException.class)
  public void removeOrganization_zkFails_exceptionRethrown() throws Exception {
    doThrow(new Exception()).when(zkClient).deleteZnode(ORG_ID);
    zookeeperGateway.removeOrganization(ORG_ID);
    verify(zkClient).deleteZnode(ORG_ID);
  }

  @Test
  public void removeUserFromOrg_orgExists_aclRevoked() throws Exception {
    zookeeperGateway.removeUserFromOrg(USER, ORG_ID);
    verify(zkClient).removeUserFromAcl(ORG_ID, USER);
  }

  @Test(expected = AuthorizableGatewayException.class)
  public void removeUserFromOrg_orgNotExist_exceptionRethrown() throws Exception {
    doThrow(new KeeperException.NoNodeException()).when(zkClient).removeUserFromAcl(ORG_ID, USER);
    zookeeperGateway.removeUserFromOrg(USER, ORG_ID);
    verify(zkClient).removeUserFromAcl(ORG_ID, USER);
  }

  @Test(expected = AuthorizableGatewayException.class)
  public void removeUserFromOrg_zkFails_exceptionRethrown() throws Exception {
    doThrow(new Exception()).when(zkClient).removeUserFromAcl(ORG_ID, USER);
    zookeeperGateway.removeUserFromOrg(USER, ORG_ID);
    verify(zkClient).removeUserFromAcl(ORG_ID, USER);
  }

  @Test
  public void getName_always_returnsZookeeper() throws Exception {
    assertThat(zookeeperGateway.getName(), equalTo("zookeeper"));
  }
}
