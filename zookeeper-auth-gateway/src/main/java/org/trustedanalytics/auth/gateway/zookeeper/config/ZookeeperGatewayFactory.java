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

package org.trustedanalytics.auth.gateway.zookeeper.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.trustedanalytics.auth.gateway.spi.Authorizable;
import org.trustedanalytics.auth.gateway.zookeeper.ZookeeperGateway;
import org.trustedanalytics.auth.gateway.zookeeper.client.KerberosfulZookeeperClient;
import org.trustedanalytics.auth.gateway.zookeeper.client.KerberoslessZookeeperClient;
import org.trustedanalytics.auth.gateway.zookeeper.client.ZookeeperClient;
import org.trustedanalytics.auth.gateway.zookeeper.kerberos.KerberosClient;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.function.Function;

public final class ZookeeperGatewayFactory {

    private final ZookeeperAuthorizationEnv env;
    private final KerberosClient kerberosClient;
    private final Function<CuratorFrameworkFactory.Builder, CuratorFrameworkFactory.Builder>
        customCuratorBuilderSteps;

    public static ZookeeperGatewayFactory newInstance(ZookeeperAuthorizationEnv env,
        KerberosClient kerberosClient,
        Function<CuratorFrameworkFactory.Builder, CuratorFrameworkFactory.Builder> customCuratorBuilderSteps) {
        return new ZookeeperGatewayFactory(env, kerberosClient, customCuratorBuilderSteps);
    }

    private ZookeeperGatewayFactory(ZookeeperAuthorizationEnv env,
        KerberosClient kerberosClient,
        Function<CuratorFrameworkFactory.Builder, CuratorFrameworkFactory.Builder> customCuratorBuilderSteps) {
        this.env = env;
        this.kerberosClient = kerberosClient;
        this.customCuratorBuilderSteps = customCuratorBuilderSteps;
    }

    public Authorizable create() throws IOException, LoginException {
        return new ZookeeperGateway(zkClient(), env.kerberosProperties().getUser());
    }

    private ZookeeperClient zkClient() throws IOException, LoginException {
        if (kerberosClient.login(env.kerberosProperties())) {
            return new KerberosfulZookeeperClient(curator(), env.zookeeperRootNode());
        } else {
            return new KerberoslessZookeeperClient(curator(), env.zookeeperRootNode());
        }
    }

    private CuratorFramework curator() {
        CuratorFrameworkFactory.Builder clientBuilder = CuratorFrameworkFactory.builder()
            .connectString(env.zookeeperQuorum())
            .retryPolicy(new ExponentialBackoffRetry(1000, 3));

        clientBuilder = customCuratorBuilderSteps.apply(clientBuilder);

        CuratorFramework client = clientBuilder.build();
        client.start();
        return client;
    }
}
