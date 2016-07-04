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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.trustedanalytics.auth.gateway.zookeeper.client.KerberoslessZookeeperClient;
import org.trustedanalytics.auth.gateway.zookeeper.client.ZookeeperClient;
import org.trustedanalytics.auth.gateway.zookeeper.integration.zkoperations.ZookeeperTestOperations;

import java.io.IOException;

public class WithoutSASLIntegrationTest extends IntegrationTestBase {

    private static TestingServer zookeeperTestServer;

    @BeforeClass
    public static void classSetup() throws Exception {
        zookeeperTestServer = new TestingServer();
    }

    @AfterClass
    public static void classTearDown() throws IOException {
        zookeeperTestServer.stop();
    }

    @Override
    protected ZookeeperTestOperations getZkTestOperations() {
        return ZookeeperTestOperations.withoutSASLChecking(getZkTestServerConnectionString());
    }

    @Override
    protected String getZkTestServerConnectionString() {
        return zookeeperTestServer.getConnectString();
    }


    @Override
    protected ZookeeperClient getZookeeperClient(String rootNode) {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder().retryPolicy(new RetryOneTime(1))
                .connectString(getZkTestServerConnectionString()).build();
        curatorFramework.start();
        return new KerberoslessZookeeperClient(curatorFramework, rootNode);
    }
}
