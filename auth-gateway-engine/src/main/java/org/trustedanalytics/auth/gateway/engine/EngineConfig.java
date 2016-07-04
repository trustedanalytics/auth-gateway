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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.trustedanalytics.auth.gateway.cloud.Cloud;
import org.trustedanalytics.auth.gateway.spi.Authorizable;
import org.trustedanalytics.auth.gateway.state.State;

import java.util.List;

@Configuration
class EngineConfig {

    @Value("${engine.timeout}")
    private long timeout;

    /**
     * IoC Container collects all Beans implementing Authorizable interface and injects here.
     */
    @Autowired
    private List<Authorizable> supportedAuthorizables;

    @Autowired
    private Cloud cloud;

    @Autowired
    private State state;

    @Bean
    public Engine getEngine() {
    return new Engine(supportedAuthorizables, timeout, cloud, state);
    }

}
