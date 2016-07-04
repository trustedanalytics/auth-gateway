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

import org.trustedanalytics.auth.gateway.spi.Authorizable;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;
import org.trustedanalytics.auth.gateway.zookeeper.client.ZookeeperClient;
import org.trustedanalytics.auth.gateway.zookeeper.client.ZookeeperPermission;

public class ZookeeperGateway implements Authorizable {

  private final ZookeeperClient zkClient;
  private final String superUser;

  public ZookeeperGateway(ZookeeperClient zkClient, String superUser) {
    this.zkClient = zkClient;
    this.superUser = superUser;
  }

  @Override
  public void addOrganization(String orgId) throws AuthorizableGatewayException {
    try {
      zkClient.createZnode(orgId, superUser, ZookeeperPermission.CRDWA);
    } catch (Exception e) {
      throw new AuthorizableGatewayException(failMsg("creating znode"), e);
    }
  }

  @Override
  public void addUserToOrg(String userId, String orgId) throws AuthorizableGatewayException {
    try {
      zkClient.addUserToAcl(orgId, userId, ZookeeperPermission.CRDW);
    } catch (Exception e) {
      throw new AuthorizableGatewayException(failMsg("modifying znode ACLs"), e);
    }
  }

  @Override
  public void removeOrganization(String orgId) throws AuthorizableGatewayException {
    try {
      zkClient.deleteZnode(orgId);
    } catch (Exception e) {
      throw new AuthorizableGatewayException(failMsg("deleting znode"), e);
    }
  }

  @Override
  public void removeUserFromOrg(String userId, String orgId) throws AuthorizableGatewayException {
    try {
      zkClient.removeUserFromAcl(orgId, userId);
    } catch (Exception e) {
      throw new AuthorizableGatewayException(failMsg("modifying znode ACLs"), e);
    }
  }

  @Override
  public void synchronize() throws AuthorizableGatewayException {}

  private String failMsg(String failureReason) {
    return "Zookeeper auth gateway failed on " + failureReason;
  }

  @Override
  public String getName() {
    return "zookeeper";
  }
}
