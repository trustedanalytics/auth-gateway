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
package org.trustedanalytics.auth.gateway.hdfs.kerberos;

import org.trustedanalytics.auth.gateway.KeyTab;
import org.trustedanalytics.auth.gateway.SystemEnvironment;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.krb5.KrbException;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class LoggedInKerberosClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggedInKerberosClient.class);

  private KerberosProperties kerberosProperties;

  private Configuration configuration;

  public LoggedInKerberosClient(KerberosProperties kerberosProperties) throws IOException,
      LoginException, KrbException {
    this.kerberosProperties = kerberosProperties;
    this.loginInToHadoop();
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  private void loginInToHadoop() throws LoginException, KrbException, IOException {
    LOGGER.info(String.format("Trying authorization with kerberos as: %s using keytab",
        kerberosProperties.getKeytabPrincipal()));

    KrbLoginManager loginManager =
        KrbLoginManagerFactory.getInstance().getKrbLoginManagerInstance(
            kerberosProperties.getKdc(), kerberosProperties.getRealm());
    configuration = new SystemEnvironment().getHadoopConfiguration();

    loginManager.loginInHadoop(loginManager.loginWithKeyTab(
        kerberosProperties.getKeytabPrincipal(),
        KeyTab.createInstance(kerberosProperties.getKeytab(),
            kerberosProperties.getKeytabPrincipal()).getFullKeyTabFilePath()), configuration);
  }
}
