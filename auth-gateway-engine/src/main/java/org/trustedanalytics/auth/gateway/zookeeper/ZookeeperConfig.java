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

import lombok.Getter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.trustedanalytics.auth.gateway.KeyTab;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;

import java.nio.charset.Charset;

@Configuration
public class ZookeeperConfig {

    @Value("${zookeeper.clusterUrl}")
    @Getter
    private String quorum;

    @Value("${zookeeper.user}")
    @Getter
    private String username;

    @Value("${zookeeper.password}")
    @Getter
    private String password;

    @Value("${zookeeper.node}")
    @Getter
    private String node;

    @Value("${kerberos.enabled}")
    @Getter
    private boolean kerberos;

    @Value("${kerberos.user}")
    @Getter
    private String kerberosUser;

    @Value("${kerberos.password}")
    @Getter
    private String kerberosPassword;

    @Value("${kerberos.kdc}")
    @Getter
    private String kdc;

    @Value("${kerberos.realm}")
    @Getter
    private String realm;

    private void logWithCredentials() throws Exception
    {
        System.setProperty("zookeeper.sasl.clientconfig", kerberosUser);
        KrbLoginManager loginManager = KrbLoginManagerFactory.getInstance()
                .getKrbLoginManagerInstance(kdc, realm);
        loginManager.loginWithCredentials(kerberosUser, kerberosPassword.toCharArray());
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework getCuratorClient() throws Exception
    {
        CuratorFrameworkFactory.Builder builder =  CuratorFrameworkFactory.builder()
                .connectString(quorum).retryPolicy(new ExponentialBackoffRetry(1000, 3));
        if(kerberos)
            logWithCredentials();
        else
            builder.authorization("digest", String.format("%s:%s", this.username, this.password)
                    .getBytes(Charset.defaultCharset()));

        return builder.build();
    }
}
