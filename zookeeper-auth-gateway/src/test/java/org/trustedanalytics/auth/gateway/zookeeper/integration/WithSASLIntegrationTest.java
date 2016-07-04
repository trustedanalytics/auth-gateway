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
import org.apache.curator.retry.RetryNTimes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.trustedanalytics.auth.gateway.zookeeper.client.KerberosfulZookeeperClient;
import org.trustedanalytics.auth.gateway.zookeeper.client.ZookeeperClient;
import org.trustedanalytics.auth.gateway.zookeeper.integration.testserver.SaslTestingServer;
import org.trustedanalytics.auth.gateway.zookeeper.integration.zkoperations.ZookeeperTestOperations;

import java.io.IOException;
import java.util.function.Function;

public class WithSASLIntegrationTest extends IntegrationTestBase {

  private static SaslTestingServer zookeeperTestServer;

  @BeforeClass
  public static void classSetup() throws Exception {
    zookeeperTestServer = new SaslTestingServer();
  }

  @AfterClass
  public static void classTearDown() throws IOException {
    zookeeperTestServer.stop();
  }

  @Override
  protected ZookeeperTestOperations getZkTestOperations() {
    return ZookeeperTestOperations.withSASLChecking(getZkTestServerConnectionString());
  }

  @Override
  protected String getZkTestServerConnectionString() {
    return zookeeperTestServer.getConnectionString();
  }

    @Override
    protected ZookeeperClient getZookeeperClient(String rootNode) {
        Function<CuratorFrameworkFactory.Builder, CuratorFrameworkFactory.Builder> customBuilder = Function.identity();
        CuratorFramework curatorFramework = customBuilder.apply(CuratorFrameworkFactory.builder().retryPolicy(new RetryNTimes(3, 1000))
                .connectString(getZkTestServerConnectionString()).authorization("backdoor", null)).build();
        curatorFramework.start();
        return new KerberosfulZookeeperClient(curatorFramework, rootNode);
    }
}
