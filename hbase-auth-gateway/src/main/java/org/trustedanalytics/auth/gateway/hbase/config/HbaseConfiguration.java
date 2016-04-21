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

package org.trustedanalytics.auth.gateway.hbase.config;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.hbase.security.UserProvider;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.auth.gateway.SystemEnvironment;
import org.trustedanalytics.auth.gateway.hbase.kerberos.KerberosHbaseProperties;
import org.trustedanalytics.cfbroker.config.HadoopZipConfiguration;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;

@Profile("hbase-auth-gateway")
@org.springframework.context.annotation.Configuration
public class HbaseConfiguration {

  private final static Logger LOGGER = LoggerFactory.getLogger(HbaseConfiguration.class);

  private final static String AUTHENTICATION_METHOD = "kerberos";

  private final static String AUTHENTICATION_METHOD_PROPERTY = "hbase.security.authentication";

  @Autowired
  private KerberosHbaseProperties kerberosHbaseProperties;

  @Value("${hbase.provided.zip}")
  private String hbaseProvidedZip;

  @Bean(destroyMethod = "close")
  public Connection getHBaseConnection() throws InterruptedException, URISyntaxException,
      LoginException, IOException {

    Configuration hbaseConfiguration =
        HadoopZipConfiguration.createHadoopZipConfiguration(hbaseProvidedZip)
            .getAsHadoopConfiguration();

    if (AUTHENTICATION_METHOD.equals(hbaseConfiguration.get(AUTHENTICATION_METHOD_PROPERTY))) {
      LOGGER.info("Creating hbase client with kerberos support");
      return getSecuredHBaseClient(hbaseConfiguration);
    } else {
      LOGGER.info("Creating hbase client without kerberos support");
      return getUnsecuredHBaseClient(hbaseConfiguration);
    }
  }

  private Connection getUnsecuredHBaseClient(Configuration hbaseConf) throws InterruptedException,
      URISyntaxException, LoginException, IOException {
    SystemEnvironment systemEnvironment = new SystemEnvironment();
    Configuration conf = HBaseConfiguration.create(hbaseConf);
    User user =
        UserProvider.instantiate(hbaseConf).create(
            UserGroupInformation.createRemoteUser(systemEnvironment
                .getVariable(SystemEnvironment.KRB_USER)));
    Connection connection = ConnectionFactory.createConnection(conf, user);
    return connection;
  }

  private Connection getSecuredHBaseClient(Configuration hbaseConf) throws InterruptedException,
      URISyntaxException, LoginException, IOException {
    LOGGER.info("Trying kerberos authentication");
    KrbLoginManager loginManager =
        KrbLoginManagerFactory.getInstance().getKrbLoginManagerInstance(
            kerberosHbaseProperties.getKdc(), kerberosHbaseProperties.getRealm());

    SystemEnvironment systemEnvironment = new SystemEnvironment();
    Subject subject =
        loginManager.loginWithCredentials(
            systemEnvironment.getVariable(SystemEnvironment.KRB_USER), systemEnvironment
                .getVariable(SystemEnvironment.KRB_PASSWORD).toCharArray());
    loginManager.loginInHadoop(subject, hbaseConf);
    Configuration conf = HBaseConfiguration.create(hbaseConf);
    User user =
        UserProvider.instantiate(conf).create(UserGroupInformation.getUGIFromSubject(subject));
    Connection connection = ConnectionFactory.createConnection(conf, user);

    return connection;
  }
}
