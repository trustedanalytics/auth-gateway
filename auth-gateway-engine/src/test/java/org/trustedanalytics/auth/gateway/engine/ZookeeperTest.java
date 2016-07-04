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
package org.trustedanalytics.auth.gateway.engine;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.trustedanalytics.auth.gateway.zookeeper.ZookeeperClient;

import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class ZookeeperTest {

    private static final String BASE_DIR = "/base-dir";
    private static final String ZNODE_DATA = "data";

    private ACL acl;
    private CuratorFramework curatorFramework;
    private ZookeeperClient client;
    private static TestingServer testingServer;
    private String znode;

    @Rule
    public TestName testName = new TestName();

    @Before
    public void setUp() throws Exception {
        curatorFramework = CuratorFrameworkFactory.builder().connectString(testingServer.getConnectString())
                .retryPolicy( new RetryNTimes(3, 1000)).authorization("digest", "myuser:mypass".getBytes()).build();
        curatorFramework.start();
        acl = new ACL(ZooDefs.Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest("myuser:mypass")));
        client = new ZookeeperClient(curatorFramework, acl, BASE_DIR);
        client.init();
        // different zookeeper root node for each test - to avoid test server restart between tests
        znode = "/" + getClass().getSimpleName() + "_" + testName.getMethodName();
    }

    @After
    public void cleanUp()
    {
        client.destroy();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        testingServer = new TestingServer();
        testingServer.start();
    }

    @AfterClass
    public static void destroyClass() throws Exception {
        testingServer.close();
    }

    @Test
    public void zookeeper_baseDirectory_verifyAccess() throws Exception {
        List<ACL> acls = curatorFramework.getACL().forPath(BASE_DIR);
        assertEquals(acls.size(), 1);
        assertEquals(acls.get(0), acl);
    }

    @Test
    public void zookeeper_znode_tryCreate() throws Exception {

        client.createNode(znode, ZNODE_DATA.getBytes());

        assertEquals(curatorFramework.checkExists().forPath(String.join("", BASE_DIR, znode))!= null, true);
        assertEquals(client.checkExists(znode), true);
    }

    @Test
    public void zookeeper_znode_createWithAcl() throws Exception {
        ACL newAcl = new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE);

        client.createNode(znode, ZNODE_DATA.getBytes(), newAcl);

        List<ACL> acls = curatorFramework.getACL().forPath(String.join("", BASE_DIR, znode));

        assertEquals(acls.stream().filter(a-> Objects.equals(a,newAcl)).count(), 1);
        assertEquals(acls.stream().filter(a-> Objects.equals(a,acl)).count(), 1);
    }

    @Test
    public void zookeeper_znode_createIfExists() throws Exception {
        client.createNode(znode, ZNODE_DATA.getBytes());

        client.createNode(znode, ZNODE_DATA.getBytes());

        assertEquals(curatorFramework.checkExists().forPath(String.join("", BASE_DIR, znode))!= null, true);
        assertEquals(client.checkExists(znode), true);
    }

    @Test(expected = Exception.class)
    public void zookeeper_znode_throwIfParentDoesNotExists() throws Exception {
        String znode = this.znode + "/znode/znode";
        client.createNode(znode, ZNODE_DATA.getBytes());
    }

    @Test
    public void zookeeper_deleteZnode_deleteWithChilds() throws Exception {
        String znode1 = znode + "/test";
        String znode2 = znode + "/test/test";
        String znode3 = znode + "/zoo";
        client.createNode(znode, ZNODE_DATA.getBytes());
        client.createNode(znode1, ZNODE_DATA.getBytes());
        client.createNode(znode2, ZNODE_DATA.getBytes());
        client.createNode(znode3, ZNODE_DATA.getBytes());

        client.deleteNode(znode);

        assertEquals(client.checkExists(znode), false);
        assertEquals(client.checkExists(znode1), false);
        assertEquals(client.checkExists(znode2), false);
        assertEquals(client.checkExists(znode3), false);
    }

    @Test
    public void zookeeper_deleteZnode_doNothingWhenZnodeDoesntExists() throws Exception {
        String znode2 = znode + "/test/test/test";
        client.deleteNode(znode2);
    }

    @Test
    public void zookeeper_acl_addDefaultClientPrivileges() throws Exception {
        ACL newAcl = new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE);

        client.appentDefaultAcls(newAcl);

        client.createNode(znode, ZNODE_DATA.getBytes());

        List<ACL> acls = curatorFramework.getACL().forPath(String.join("", BASE_DIR, znode));

        assertEquals(acls.stream().filter(a-> Objects.equals(a,newAcl)).count(), 1);
        assertEquals(acls.stream().filter(a-> Objects.equals(a,acl)).count(), 1);
    }

    @Test
    public void zookeeper_acl_removeDefaultClientPrivileges() throws Exception {
        ACL newAcl = new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE);

        client.appentDefaultAcls(newAcl);
        client.removeDefaultAcls(acl);

        client.createNode(znode, ZNODE_DATA.getBytes());

        List<ACL> acls = curatorFramework.getACL().forPath(String.join("", BASE_DIR, znode));

        assertEquals(acls.stream().filter(a-> Objects.equals(a,newAcl)).count(), 1);
        assertEquals(acls.stream().filter(a-> Objects.equals(a,acl)).count(), 0);
    }

    @Test
    public void zookeeper_data_testZNodeData() throws Exception {
        client.createNode(znode, ZNODE_DATA.getBytes());

        assertEquals(ZNODE_DATA, new String(client.getNodeData(znode)));
    }

    @Test
    public void zookeeper_data_setZNodeData() throws Exception {
        client.createNode(znode, ZNODE_DATA.getBytes());

        client.setNodeData(znode, "test".getBytes());

        assertEquals("test", new String(client.getNodeData(znode)));
    }
}
