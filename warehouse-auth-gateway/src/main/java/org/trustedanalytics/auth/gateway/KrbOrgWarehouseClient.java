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
package org.trustedanalytics.auth.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.trustedanalytics.auth.gateway.hive.HiveClientFactory;
import org.trustedanalytics.auth.gateway.impala.ImpalaClientFactory;
import org.trustedanalytics.auth.gateway.sentry.SentryClientFactory;
import org.trustedanalytics.auth.gateway.spi.Authorizable;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;
import org.trustedanalytics.auth.gateway.utils.Qualifiers;

@Component
@Profile(Qualifiers.KERBEROS)
public class KrbOrgWarehouseClient implements Authorizable {

  public static final String WAREHOUSE = "krb-warehouse";

  @Autowired
  private SentryClientFactory sentryFactory;

  @Autowired
  private HiveClientFactory hiveFactory;

  @Autowired
  private ImpalaClientFactory impalaFactory;

  @Override
  public void addOrganization(String orgId) throws AuthorizableGatewayException {
    sentryFactory.createRequest(sentryClient -> {
      sentryClient.createRole(orgId);
      sentryClient.grantRoleToGroup(orgId, orgId);
    });

    hiveFactory.createRequest(hiveClient -> {
      hiveClient.createDatabase(orgId);
      hiveClient.grantPrivileges(orgId, orgId);
      hiveClient.grantPrivilegesForUri(orgId, orgId);
    });

    impalaFactory.createRequest(impalaClient -> impalaClient.invalidateMetadata());
  }

  @Override
  public void addUserToOrg(String userId, String orgId) throws AuthorizableGatewayException {}

  @Override
  public void removeOrganization(String orgId) throws AuthorizableGatewayException {
    sentryFactory.createRequest(sentryClient -> sentryClient.dropRoleIfExists(orgId));
  }

  @Override
  public void removeUserFromOrg(String userId, String orgId) throws AuthorizableGatewayException {}

  @Override
  public void synchronize() throws AuthorizableGatewayException {}

  @Override
  public String getName() {
    return WAREHOUSE;
  }
}
