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

package org.trustedanalytics.auth.gateway.zookeeper.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KerberoslessZookeeperClient implements ZookeeperClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(KerberoslessZookeeperClient.class);

    private final CuratorFramework curatorClient;
    private final PathOperations pathOps;

    public KerberoslessZookeeperClient(CuratorFramework curatorClient, String rootNode) {
        this.curatorClient = curatorClient;
        this.pathOps = new PathOperations(rootNode);

        //TODO: verify rootNode format and existence - here or in init method
    }

    @Override
    public void createZnode(String znodePath, String content, String username,
        ZookeeperPermission permission) throws Exception {

        try {
            curatorClient.create()
                .forPath(pathOps.makePath(znodePath), content.getBytes());
        } catch (KeeperException.NodeExistsException e) {
            LOGGER.info(
                "Caught: '" + e.getMessage() + "' while creating node. Nothing to do.");
        }
    }

    @Override
    public void deleteZnode(String znodePath) throws Exception {
        try {
            curatorClient.delete()
                .forPath(pathOps.makePath(znodePath));
        } catch (KeeperException.NoNodeException e) {
            LOGGER.info(
                "Caught: '" + e.getMessage() + "' while deleting node. Nothing to do.");
        }
    }

    @Override
    public void addUserToAcl(String znodePath, String username, ZookeeperPermission permission)
        throws Exception {
        //no-operation
    }

    @Override
    public void removeUserFromAcl(String znodePath, String username) {
        //no-operation
    }
}
