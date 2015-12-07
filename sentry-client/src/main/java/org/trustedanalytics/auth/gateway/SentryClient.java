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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.trustedanalytics.auth.gateway.spi.Authorizable;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

@Component
@Profile("sentry-auth-gateway")
public class SentryClient implements Authorizable {

  public static final String SENTRY = "sentry";

  @Autowired
  private CustomSentryPolicyServiceClient delegate;

  @Override
  public void addOrganization(String orgId, String orgName) throws AuthorizableGatewayException {
      delegate.createRole(orgName);
      delegate.grantRoleToGroup(orgName, orgName);
  }

  @Override
  public void addUser(String userId, String userName) throws AuthorizableGatewayException {
  }

  @Override
  public void addUserToOrg(String userId, String orgId) throws AuthorizableGatewayException {
  }

  @Override
  public void removeOrganization(String orgId, String orgName) throws AuthorizableGatewayException {
      delegate.dropRoleIfExists(orgName);
  }

  @Override
  public void removeUser(String userId, String userName) throws AuthorizableGatewayException {
  }

  @Override
  public void removeUserFromOrg(String userId, String orgId) throws AuthorizableGatewayException {

  }

  @Override
  public String getName() {
    return SENTRY;
  }
}