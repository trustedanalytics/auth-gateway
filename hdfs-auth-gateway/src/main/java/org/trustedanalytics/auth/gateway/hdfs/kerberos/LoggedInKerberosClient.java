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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.security.auth.login.LoginException;

import com.google.common.annotations.VisibleForTesting;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.hadoop.config.client.Configurations;
import org.trustedanalytics.hadoop.config.client.ServiceType;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;

import sun.security.krb5.KrbException;

public class LoggedInKerberosClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggedInKerberosClient.class);

  private final String keyTabFilePath;

  private KerberosProperties kerberosProperties;

  private Configuration configuration;

  public LoggedInKerberosClient(KerberosProperties kerberosProperties) throws IOException,
      LoginException, KrbException {
    this.keyTabFilePath = "/tmp/superuser.keytab";
    this.kerberosProperties = kerberosProperties;
    this.saveKeytabIfNotExists();
    this.loginInToHadoop();
  }

  @VisibleForTesting
  LoggedInKerberosClient(KerberosProperties kerberosProperties, String path) throws IOException {
    this.kerberosProperties = kerberosProperties;
    this.keyTabFilePath = path;
    this.saveKeytabIfNotExists();
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
    configuration =
        Configurations.newInstanceFromEnv().getServiceConfig(ServiceType.HDFS_TYPE)
            .asHadoopConfiguration();
    loginManager.loginInHadoop(
        loginManager.loginWithKeyTab(kerberosProperties.getKeytabPrincipal(), keyTabFilePath),
        configuration);
  }

  private void saveKeytabIfNotExists() throws IOException {
    Path path = Paths.get(keyTabFilePath);
    if (Files.notExists(path)) {
      Files.write(path, kerberosProperties.getKeytab(), StandardOpenOption.CREATE_NEW);
    } else if (Files.isDirectory(path)) {
      throw new IOException(
          String
              .format(
                  "Under path %s exists directory. It's path where hdfs superuser keytab is stored. Please move or delete this directory",
                  keyTabFilePath));
    }
  }
}
