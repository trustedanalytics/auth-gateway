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
package org.trustedanalytics.auth.gateway.impala;

import java.io.IOException;

import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.trustedanalytics.auth.gateway.configuration.Authenticator;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;
import org.trustedanalytics.auth.gateway.utils.ClientInterface;
import org.trustedanalytics.auth.gateway.utils.Qualifiers;

@Component
@Profile({Qualifiers.SIMPLE, Qualifiers.KERBEROS})
public class ImpalaClientFactory {

  @Autowired
  private ImpalaClientConfiguration configuration;

  @Autowired
  private Authenticator authenticator;

  private ImpalaClient createClient() throws AuthorizableGatewayException {
    try {
      UserGroupInformation ugi = authenticator.getUserUGI();
      return new ImpalaClient.Builder(ugi).connectionUrl(configuration.getConnectionUrl())
          .available(configuration.getAvailable()).build();
    } catch (Exception e) {
      throw new AuthorizableGatewayException("Cannot create impala client", e);
    }
  }

  public void createRequest(ClientInterface<ImpalaClient> impalaClientInterface)
      throws AuthorizableGatewayException {
    try (ImpalaClient client = createClient()) {
      impalaClientInterface.execute(client);
    } catch (IOException e) {
      throw new AuthorizableGatewayException("Unknown impala client error", e);
    }
  }
}
