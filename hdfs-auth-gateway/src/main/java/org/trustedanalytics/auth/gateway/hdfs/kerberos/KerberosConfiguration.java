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

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.auth.gateway.hdfs.config.ExternalConfiguration;
import org.trustedanalytics.auth.gateway.hdfs.utils.Qualifiers;
import org.trustedanalytics.hadoop.config.client.Configurations;
import org.trustedanalytics.hadoop.config.client.Property;
import org.trustedanalytics.hadoop.config.client.ServiceInstanceConfiguration;

@Profile(Qualifiers.HDFS)
@Configuration
public class KerberosConfiguration {

  public static final String KERBEROS_SERVICE = "kerberos-service";

  @Autowired
  private ExternalConfiguration externalConfiguration;

  @Bean
  @Profile(Qualifiers.TEST_EXCLUDE)
  public KerberosProperties getKerberosProperties() throws IOException {
    ServiceInstanceConfiguration krbConf =
        Configurations.newInstanceFromEnv().getServiceConfig(KERBEROS_SERVICE);
    String kdc = krbConf.getProperty(Property.KRB_KDC).get();
    String realm = krbConf.getProperty(Property.KRB_REALM).get();
    String technicalUser = krbConf.getProperty(Property.USER).get();
    String principal = externalConfiguration.getSuperUser();
    String keytabFile = externalConfiguration.getKeytab();

    return new KerberosProperties(kdc, realm, technicalUser, principal, keytabFile);
  }
}
