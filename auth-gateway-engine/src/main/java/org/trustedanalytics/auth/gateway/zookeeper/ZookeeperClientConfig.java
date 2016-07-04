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

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.security.NoSuchAlgorithmException;

@Configuration
public class ZookeeperClientConfig {

    @Autowired
    private CuratorFramework curator;

    @Autowired
    private ZookeeperConfig zookeeperConfig;

    public ZookeeperClientConfig() {
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public ZookeeperClient getZookeeperClient() throws NoSuchAlgorithmException{
        ACL acl;
        if (zookeeperConfig.isKerberos())
            acl = new ACL(ZooDefs.Perms.ALL, new Id("sasl", zookeeperConfig.getUsername()));
        else
            acl = new ACL(ZooDefs.Perms.ALL, new Id("digest",
                    DigestAuthenticationProvider.generateDigest(String.format("%s:%s",
                            zookeeperConfig.getUsername(), zookeeperConfig.getPassword()))));
        ZookeeperClient client = new ZookeeperClient(curator, acl, zookeeperConfig.getNode());
        return client;
    }
}
