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
package org.trustedanalytics.auth.gateway.engine.integration.tests;

import org.apache.curator.test.TestingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

@Configuration
@EnableAuthorizationServer
@SuppressWarnings("static-method")
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationServerConfig.class);

  @Autowired
  private TestingServer zookeeperServerFactory;

  @Value("${jwt.token.publicKey}")
  private String publicKey;

  @Value("${jwt.token.privateKey}")
  private String privateKey;

  @Bean
  public JwtAccessTokenConverter accessTokenConverter() throws Exception {
    JwtAccessTokenConverter jwt = new JwtAccessTokenConverter();
    jwt.setSigningKey(privateKey);
    jwt.setVerifierKey(publicKey);
    jwt.afterPropertiesSet();
    return jwt;
  }

  @Autowired
  private AuthenticationManager authenticationManager;

  @Override
  public void configure(final AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    endpoints
        .authenticationManager(authenticationManager)
        .accessTokenConverter(accessTokenConverter());
  }

  @Override
  public void configure(final ClientDetailsServiceConfigurer clients) throws Exception {
    clients.inMemory()
        .withClient(AuthGatewayControllerTest.ADMIN_NAME)
        .scopes("cloud_controller.admin")
        .and()
        .withClient(AuthGatewayControllerTest.ORG_MANAGER_NAME)
        .scopes("cloud_controller.write", "cloud_controller.read");
  }
}
