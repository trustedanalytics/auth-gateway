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
package org.trustedanalytics.auth.gateway.sentry;

import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.trustedanalytics.auth.gateway.configuration.Authenticator;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;
import org.trustedanalytics.auth.gateway.utils.ClientInterface;
import org.trustedanalytics.auth.gateway.utils.Qualifiers;

@Component
@Profile(Qualifiers.KERBEROS)
public class SentryClientFactory {

  @Autowired
  private SentryClientConfiguration configuration;

  @Autowired
  private Authenticator authenticator;


  private SentryClient createClient() throws AuthorizableGatewayException {
    try {
      UserGroupInformation ugi = authenticator.getUserUGI();
      return new SentryClient.Builder(ugi).address(configuration.getAddress())
          .port(configuration.getPort()).principal(configuration.getPrincipal())
          .realm(authenticator.getRealm()).superUser(authenticator.getSuperUser()).build();
    } catch (Exception e) {
      throw new AuthorizableGatewayException("Cannot create sentry client", e);
    }
  }

  public void createRequest(ClientInterface<SentryClient> sentryClientInterface)
      throws AuthorizableGatewayException {
    try (SentryClient client = createClient()) {
      sentryClientInterface.execute(client);
    }
  }
}
