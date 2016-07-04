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
package org.trustedanalytics.auth.gateway.cloud;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.auth.gateway.zookeeper.ZookeeperClient;

@Configuration
@Profile("cloud")
public class CloudConfig {

    @Value("${cloud.api}")
    private String cloudApiUri;

    @Autowired
    private OAuth2RequestInterceptor interceptor;

    public CloudConfig() {
    }

    @Bean
    public Cloud getCloud() {
        return new Cloud(Feign.builder().decoder(new JacksonDecoder()).requestInterceptor(interceptor)
                .target(CloudApi.class, cloudApiUri));
    }
}
