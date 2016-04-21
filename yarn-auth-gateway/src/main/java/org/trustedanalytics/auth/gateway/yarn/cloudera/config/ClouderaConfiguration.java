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
package org.trustedanalytics.auth.gateway.yarn.cloudera.config;

import javax.validation.constraints.NotNull;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("yarn-auth-gateway")
@Configuration
public class ClouderaConfiguration {

  @Value("${yarn.cloudera.user}")
  @NotNull
  @Getter
  private String user;

  @Value("${yarn.cloudera.password}")
  @NotNull
  @Getter
  private String password;

  @Value("${yarn.cloudera.host}")
  @NotNull
  @Getter
  private String host;

  @Value("${yarn.cloudera.port}")
  @NotNull
  @Getter
  private Integer port;

}
