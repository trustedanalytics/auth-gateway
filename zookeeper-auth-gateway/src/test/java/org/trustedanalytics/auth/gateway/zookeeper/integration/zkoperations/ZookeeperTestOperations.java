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

package org.trustedanalytics.auth.gateway.zookeeper.integration.zkoperations;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class ZookeeperTestOperations implements Closeable {

    private static boolean saslChecking;

    public static ZookeeperTestOperations withSASLChecking(String zkConnectionString) {
        return newInstance(zkConnectionString, true);
    }

    public static ZookeeperTestOperations withoutSASLChecking(String zkConnectionString) {
        return newInstance(zkConnectionString, false);
    }

    private final CuratorFramework curator;

    private static ZookeeperTestOperations newInstance(String zkConnectionString,
        boolean saslChecking) {
        ZookeeperTestOperations.saslChecking = saslChecking;
        CuratorFramework curator = CuratorFrameworkFactory.builder()
            .connectString(zkConnectionString)
            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
            .build();
        return new ZookeeperTestOperations(curator);
    }

    private ZookeeperTestOperations(CuratorFramework curator) {
        this.curator = curator;
        curator.start();
    }

    @Override
    public void close() throws IOException {
        curator.close();
    }

    public void createNode(String nodePath) throws Exception {
        curator.create()
            .creatingParentsIfNeeded()
            .forPath(nodePath);
    }

    public void assertNodeExists(String nodePath, List<ACL> acls) throws Exception {
        if (saslChecking) {
            List<ACL> actualAcls = curator.getACL().forPath(nodePath);
            assertThat(actualAcls, equalTo(acls));
        } else {
            Stat stat = curator.checkExists().forPath(nodePath);
            assertNotNull(stat);
        }
    }

    public void assertNodeNotExist(String nodePath) throws Exception {
        Stat stat = curator.checkExists().forPath(nodePath);
        assertNull(stat);
    }
}
