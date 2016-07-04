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
package org.trustedanalytics.auth.gateway.yarn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.auth.gateway.spi.Authorizable;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

@Profile("yarn-auth-gateway")
@Configuration
public class YarnGateway implements Authorizable {

  private static final Logger LOGGER = LoggerFactory.getLogger(YarnGateway.class);

  private static final String NAME = "yarn";

  @Autowired
  private YarnApiClient yarnApiClient;

  @Override
  public void addOrganization(String orgId) throws AuthorizableGatewayException {
    LOGGER.debug(String.format("Create organization %s", orgId));
    try {
      yarnApiClient.addQueue(orgId);
    } catch (Exception e) {
      throw new AuthorizableGatewayException(String.format("Can't add organization: %s", orgId), e);
    }
  }

  @Override
  public void addUserToOrg(String userId, String orgId) throws AuthorizableGatewayException {}

  @Override
  public void removeOrganization(String orgId) throws AuthorizableGatewayException {
    LOGGER.debug(String.format("Remove organization %s", orgId));
    try {
      yarnApiClient.deleteQueue(orgId);
    } catch (Exception e) {
      throw new AuthorizableGatewayException(String.format("Can't remove organization: %s", orgId),
          e);
    }
  }

  @Override
  public void removeUserFromOrg(String userId, String orgId) throws AuthorizableGatewayException {}

  @Override
  public void synchronize() throws AuthorizableGatewayException {}

  @Override
  public String getName() {
    return NAME;
  }
}
