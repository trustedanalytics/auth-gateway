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
package org.trustedanalytics.auth.gateway.configuration.kerberos;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.trustedanalytics.auth.gateway.KeyTab;
import org.trustedanalytics.auth.gateway.SystemEnvironment;
import org.trustedanalytics.auth.gateway.configuration.simple.WarehouseClientConfiguration;
import org.trustedanalytics.auth.gateway.utils.Qualifiers;

@Component
@Profile(Qualifiers.KERBEROS)
public class WarehouseKrbClientConfiguration {

  private String kdc;

  private String realm;

  @Value("${warehouse.client.clientKeyTab}")
  private String clientKeyTab;

  private String keyTabPath;

  @Autowired
  private WarehouseClientConfiguration simpleConfig;

  @PostConstruct
  public void initialize() throws IOException {
    SystemEnvironment systemEnvironment = new SystemEnvironment();
    kdc = systemEnvironment.getVariable(SystemEnvironment.KRB_KDC);
    realm = systemEnvironment.getVariable(SystemEnvironment.KRB_REALM);
    keyTabPath =
        KeyTab.createInstance(clientKeyTab, simpleConfig.getSuperUser()).getFullKeyTabFilePath();
  }

  public String getKdc() {
    return kdc;
  }

  public String getRealm() {
    return realm;
  }

  public WarehouseClientConfiguration getSimpleConfig() {
    return simpleConfig;
  }

  public String getKeyTabPath() {
    return keyTabPath;
  }
}
