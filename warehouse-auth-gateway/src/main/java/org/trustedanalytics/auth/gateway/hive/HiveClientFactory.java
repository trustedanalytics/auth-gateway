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
package org.trustedanalytics.auth.gateway.hive;

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
public class HiveClientFactory {

  @Autowired
  private HiveClientConfiguration configuration;

  @Autowired
  private Authenticator authenticator;

  public HiveClientFactory() {

  }

  private HiveClient createClient() throws AuthorizableGatewayException {
    try {
      UserGroupInformation ugi = authenticator.getUserUGI();
      return new HiveClient.Builder(ugi).connectionUrl(configuration.getConnectionUrl())
          .hdfsUri(configuration.getHdfsUri()).build();
    } catch (Exception e) {
      throw new AuthorizableGatewayException("Cannot create hive client", e);
    }
  }

  public void createRequest(ClientInterface<HiveClient> hiveClientInterface)
      throws AuthorizableGatewayException {
    try (HiveClient client = createClient()) {
      hiveClientInterface.execute(client);
    } catch (IOException e) {
      throw new AuthorizableGatewayException("Unknown hive client error", e);
    }
  }
}
