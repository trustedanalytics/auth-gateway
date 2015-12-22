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
package org.trustedanalytics.auth.gateway;

import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.hadoop.config.client.Configurations;
import org.trustedanalytics.hadoop.config.client.Property;
import org.trustedanalytics.hadoop.config.client.ServiceInstanceConfiguration;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;

import sun.security.krb5.KrbException;

import java.io.IOException;

import javax.security.auth.login.LoginException;

@Configuration
@ConfigurationProperties("sentry.server")
@Profile("sentry-auth-gateway")
public class SentryClientConfiguration {

  private String address;

  private int port;

  private String principal;

  @Bean(initMethod = "initialize", destroyMethod = "destroy")
  public CustomSentryPolicyServiceClient sentryPolicyServiceClient(SentryAuthenticator authenticator)
      throws IOException, LoginException, KrbException {
    UserGroupInformation ugi = authenticator.sentryUserUGI();
    String realm  = authenticator.getRealm();
    String sentrySuperUser = authenticator.getSuperUser();
    return new CustomSentryPolicyServiceClient.Builder(ugi)
               .address(address)
               .port(port)
               .principal(principal)
               .realm(realm)
               .superUser(sentrySuperUser)
           .build();
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getPrincipal() {
    return principal;
  }

  public void setPrincipal(String principal) {
    this.principal = principal;
  }
}
