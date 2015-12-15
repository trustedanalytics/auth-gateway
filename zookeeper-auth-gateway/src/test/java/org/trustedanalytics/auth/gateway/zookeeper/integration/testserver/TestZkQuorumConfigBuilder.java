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

package org.trustedanalytics.auth.gateway.zookeeper.integration.testserver;

import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.QuorumConfigBuilder;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;

import java.util.Properties;

public class TestZkQuorumConfigBuilder extends QuorumConfigBuilder {

  private final InstanceSpec instanceSpec;

  public TestZkQuorumConfigBuilder(InstanceSpec instanceSpec) {
    super(instanceSpec);
    this.instanceSpec = instanceSpec;
  }

  @Override
  public QuorumPeerConfig buildConfig(int instanceIndex) throws Exception {
    InstanceSpec spec = instanceSpec;

    Properties properties = new Properties();
    properties.setProperty("initLimit", "10");
    properties.setProperty("syncLimit", "5");
    properties.setProperty("dataDir", spec.getDataDirectory().getCanonicalPath());
    properties.setProperty("clientPort", Integer.toString(spec.getPort()));
    int tickTime = spec.getTickTime();
    if (tickTime >= 0) {
      properties.setProperty("tickTime", Integer.toString(tickTime));
    }

    int maxClientCnxns = spec.getMaxClientCnxns();
    if (maxClientCnxns >= 0) {
      properties.setProperty("maxClientCnxns", Integer.toString(maxClientCnxns));
    }

    properties.setProperty("authProvider.1",
        "org.apache.zookeeper.server.auth.SASLAuthenticationProvider");

    properties.setProperty("authProvider.2",
        "org.trustedanalytics.auth.gateway.zookeeper.integration.testserver.BackdoorAuthProvider");

    QuorumPeerConfig config1 = new QuorumPeerConfig();
    config1.parseProperties(properties);
    return config1;
  }
}
