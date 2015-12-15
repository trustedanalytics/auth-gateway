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

package org.trustedanalytics.auth.gateway.zookeeper.kerberos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;

import javax.security.auth.login.LoginException;

public class KerberosClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(KerberosClient.class);

  public boolean login(KerberosProperties krbProperties) throws LoginException {
    if (krbProperties.isValid()) {
      LOGGER.info("Found kerberos configuration - trying to authenticate");
      krbAuthenticate(krbProperties);
      return true;
    } else {
      LOGGER.warn("Kerberos configuration empty or invalid - will not try to authenticate.");
      return false;
    }
  }

  private void krbAuthenticate(KerberosProperties krbProperties) throws LoginException {
    System.setProperty("zookeeper.sasl.clientconfig", krbProperties.getUser());
    KrbLoginManager loginManager = KrbLoginManagerFactory.getInstance()
        .getKrbLoginManagerInstance(krbProperties.getKdc(), krbProperties.getRealm());
    loginManager.loginWithCredentials(krbProperties.getUser(),
        krbProperties.getPassword().toCharArray());
  }
}
