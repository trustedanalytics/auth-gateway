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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.function.Supplier;

@Configuration
public class WebApplicationConfig extends WebMvcConfigurerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebApplicationConfig.class);

  @Autowired
  Supplier<String> tokenExtractor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    LOGGER.info("Register access control interceptor");
    registry.addInterceptor(new AdminControlInterceptor(tokenExtractor))
        .addPathPatterns("/users/**", "/organizations/**", "/synchronize**", "/state**", "/jobs/**");
  }
}
