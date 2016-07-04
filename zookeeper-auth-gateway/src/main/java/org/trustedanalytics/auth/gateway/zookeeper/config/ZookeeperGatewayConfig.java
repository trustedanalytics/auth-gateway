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

package org.trustedanalytics.auth.gateway.zookeeper.config;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.auth.gateway.spi.Authorizable;
import org.trustedanalytics.auth.gateway.zookeeper.ZookeeperGateway;
import org.trustedanalytics.auth.gateway.zookeeper.client.KerberosfulZookeeperClient;
import org.trustedanalytics.auth.gateway.zookeeper.client.KerberoslessZookeeperClient;
import org.trustedanalytics.auth.gateway.zookeeper.client.ZookeeperClient;

import javax.security.auth.login.LoginException;
import java.io.IOException;

@Configuration
@Profile("zookeeper-auth-gateway")
public class ZookeeperGatewayConfig {

  private static final String BASE_NODE = "/org";

  @Autowired
  private CuratorFramework curatorFramework;

  @Value("${kerberos.enabled}")
  private boolean kerberos;

  @Value("${zookeeper.user}")
  private String username;

  private ZookeeperClient getZookeeperClient()
  {
    if(kerberos)
      return new KerberosfulZookeeperClient(curatorFramework, BASE_NODE);
    else
      return new KerberoslessZookeeperClient(curatorFramework, BASE_NODE);
  }

  @Bean
  public Authorizable zookeeperGateway() throws IOException, LoginException {
    return new ZookeeperGateway(getZookeeperClient(), username);
  }
}
