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

import com.google.common.base.Preconditions;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static java.util.stream.Collectors.toList;

public class ZookeeperClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperClient.class);

    private CuratorFramework client;
    private List<ACL> acls = new ArrayList<>();
    private String basePath;

    public ZookeeperClient(CuratorFramework client, ACL acl, String basePath) {
        this.client = client;
        this.acls.add(acl);
        this.basePath = validatePath(basePath);
    }

    public void appentDefaultAcls(ACL acl)
    {
        acls = acls.stream().filter(acl1 -> !compareAcls(acl1, acl)).collect(toList());
        acls.add(acl);
    }

    public void removeDefaultAcls(ACL acl)
    {
        acls = acls.stream().filter(acl1 -> !compareAcls(acl1, acl)).collect(toList());
    }

    private String validatePath(String path) {
        Preconditions.checkArgument(path.startsWith("/"), "Dir must starts with \"/\"");
        Preconditions.checkArgument(path.length() > 1 && !path.endsWith("/"), "Dir can not end with \"/\"");
        return path;
    }

    private void prepareBaseDirectory() throws Exception {
        try {
            client.create().creatingParentsIfNeeded().forPath(basePath);
        } catch (KeeperException.NodeExistsException e) {
            LOGGER.info("Znode " + basePath + " already exists", e);
        }
        client.setACL().withACL(acls).forPath(basePath);
    }

    private String getPath(String path) {
        return String.format("%s%s", basePath, validatePath(path));
    }

    public void init() throws Exception {
        if(client.getState() == CuratorFrameworkState.STOPPED)
          client.start();
        prepareBaseDirectory();
    }

    public void destroy()
    {
        client.delete();
    }

    public boolean checkExists(String path) throws Exception {
        return client.checkExists().forPath(getPath(path)) != null;
    }

    public void createNode(String path, byte[] data, ACL... acls) throws Exception {
        List<ACL> mixedAcls = new ArrayList<>();
        mixedAcls.addAll(this.acls);

        for (ACL acl : acls)
            mixedAcls.add(acl);

        if (!checkExists(path)) {
            client.create().withACL(mixedAcls).forPath(getPath(path), data);
        } else {
            client.setACL().withACL(mixedAcls).forPath(getPath(path));
        }
    }

    public void deleteNode(String path) throws Exception {
        if (checkExists(path))
            client.delete().deletingChildrenIfNeeded().forPath(getPath(path));
    }

    public void setNodeData(String path, byte[] data) throws Exception {
        client.setData().forPath(getPath(path), data);
    }

    public byte[] getNodeData(String path) throws Exception {
        return client.getData().forPath(getPath(path));
    }

    private boolean compareAcls(ACL acl1, ACL acl2)
    {
        return Objects.equals(acl1.getId().getId(), acl2.getId().getId()) &&
                Objects.equals(acl1.getId().getScheme(), acl2.getId().getScheme());
    }
}
