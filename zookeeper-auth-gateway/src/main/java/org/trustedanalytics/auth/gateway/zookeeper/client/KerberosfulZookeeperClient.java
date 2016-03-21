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
package org.trustedanalytics.auth.gateway.zookeeper.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 *
 * This class is zookeeper client based on CuratorFramework.
 *
 * It assumes that Kerberos ticket is available in cache, because it uses Kerberos to ACL mangling.
 * That's why it takes CuratorFramework supplier instead of just CuratorFramework object as param:
 * CuratorFramework object should be created before each request because of potential change of
 * kerberos ticket under the hood.
 *
 */
public class KerberosfulZookeeperClient implements ZookeeperClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(KerberosfulZookeeperClient.class);

  private final CuratorFramework curatorClient;
  private final PathOperations pathOps;

  public KerberosfulZookeeperClient(CuratorFramework curatorClient, String rootNode) {
    this.curatorClient = curatorClient;
    this.pathOps = new PathOperations(rootNode);

    // TODO: verify rootNode format and existence - here or in init method
  }

  @Override
  public void createZnode(String znodePath, String username, ZookeeperPermission permission)
      throws Exception {

    try {
      curatorClient.create()
          .withACL(singletonList(new ACL(permission.getPerms(), new Id("sasl", username))))
          .forPath(pathOps.makePath(znodePath));
    } catch (KeeperException.NodeExistsException e) {
      LOGGER.info("Caught: '" + e.getMessage() + "' while creating node. Ensure ACLs are correct:", e);
      addUserToAcl(znodePath, username, permission);
    }
  }

  @Override
  public void deleteZnode(String znodePath) throws Exception {
    try {
      curatorClient.delete().forPath(pathOps.makePath(znodePath));
    } catch (KeeperException.NoNodeException e) {
      LOGGER.info("Caught: '" + e.getMessage() + "' while deleting node. Nothing to do.", e);
    }
  }

  @Override
  public void addUserToAcl(String znodePath, String username, ZookeeperPermission permission)
      throws Exception {

    LOGGER.info("Trying to get data for " + znodePath);
    LOGGER.info(new String(curatorClient.getData().forPath(pathOps.makePath(znodePath))));

    List<ACL> acls = curatorClient.getACL().forPath(pathOps.makePath(znodePath));
    acls.add(new ACL(permission.getPerms(), new Id("sasl", username)));
    LOGGER.info("Trying to set '" + acls + "' for " + znodePath);
    curatorClient.setACL().withACL(acls).forPath(pathOps.makePath(znodePath));
  }

  @Override
  public void removeUserFromAcl(String znodePath, String username) throws Exception {
    List<ACL> acls = curatorClient.getACL().forPath(pathOps.makePath(znodePath));
    acls = acls.stream().filter(acl -> notMatch(acl, username)).collect(toList());
    curatorClient.setACL().withACL(acls).forPath(pathOps.makePath(znodePath));
  }

  private boolean notMatch(ACL acl, String username) {
    return !(Objects.equals(acl.getId().getId(), username)
        && Objects.equals(acl.getId().getScheme(), "sasl"));
  }
}
