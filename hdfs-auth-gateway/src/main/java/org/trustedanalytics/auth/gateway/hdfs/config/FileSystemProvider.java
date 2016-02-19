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
package org.trustedanalytics.auth.gateway.hdfs.config;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.security.auth.login.LoginException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.auth.gateway.SystemEnvironment;
import org.trustedanalytics.auth.gateway.hdfs.kerberos.KerberosProperties;
import org.trustedanalytics.auth.gateway.hdfs.kerberos.LoggedInKerberosClient;
import org.trustedanalytics.auth.gateway.hdfs.utils.Qualifiers;

import sun.security.krb5.KrbException;

import com.google.common.base.Throwables;

@Profile(Qualifiers.HDFS)
@org.springframework.context.annotation.Configuration
public class FileSystemProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemProvider.class);

  private final static String HADOOP_DEFAULT_FS = "fs.defaultFS";

  private static final String AUTHENTICATION_METHOD = "kerberos";

  private static final String AUTHENTICATION_METHOD_PROPERTY = "hadoop.security.authentication";

  @Autowired
  private KerberosProperties kerberosProperties;

  @Autowired
  private Configuration configuration;

  public FileSystem getFileSystem() throws IOException {
    if (isKerberosEnabled(configuration)) {
      LOGGER.info("Trying to get file system with kerberos authorization");
      try {
        LoggedInKerberosClient kerberosClient = new LoggedInKerberosClient(kerberosProperties);
        configuration = kerberosClient.getConfiguration();
      } catch (KrbException | LoginException | IOException e) {
        LOGGER.error("Authorization to kerberos failed", e);
        Throwables.propagateIfPossible(e, IOException.class);
        throw new IOException("Authorization to kerberos failed", e);
      }
    }
    return getFileSystemForUser(kerberosProperties.getKeytabPrincipal(), configuration);
  }

  private FileSystem getFileSystemForUser(String user, Configuration configuration)
      throws IOException {
    LOGGER.info(String.format("Get fileSystem as : %s", user));
    FileSystem fileSystem = null;
    try {
      fileSystem =
          FileSystem.get(new URI(configuration.getRaw(HADOOP_DEFAULT_FS)), configuration, user);
      return fileSystem;
    } catch (InterruptedException | URISyntaxException | IOException e) {
      LOGGER.warn("Cannot create file system", e);
      Throwables.propagateIfPossible(e, IOException.class);
      throw new IOException("Cannot create file system", e);
    }
  }

  private boolean isKerberosEnabled(Configuration configuration) {
    return AUTHENTICATION_METHOD.equals(configuration.get(AUTHENTICATION_METHOD_PROPERTY));
  }

  @Bean
  @Profile(Qualifiers.TEST_EXCLUDE)
  public Configuration getConfiguration() throws IOException {
    return new SystemEnvironment().getHadoopConfiguration();
  }
}
