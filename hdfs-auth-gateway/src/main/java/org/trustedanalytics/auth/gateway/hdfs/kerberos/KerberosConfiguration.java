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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.auth.gateway.SystemEnvironment;
import org.trustedanalytics.auth.gateway.hdfs.config.ExternalConfiguration;
import org.trustedanalytics.auth.gateway.hdfs.utils.Qualifiers;

@Profile(Qualifiers.HDFS)
@Configuration
public class KerberosConfiguration {

  @Autowired
  private ExternalConfiguration externalConfiguration;

  @Bean
  @Profile(Qualifiers.TEST_EXCLUDE)
  public KerberosProperties getKerberosProperties() throws IOException {
    SystemEnvironment systemEnvironment = new SystemEnvironment();
    String kdc = systemEnvironment.getVariable(SystemEnvironment.KRB_KDC);
    String realm = systemEnvironment.getVariable(SystemEnvironment.KRB_REALM);
    String technicalUser = systemEnvironment.getVariable(SystemEnvironment.KRB_USER);
    String principal = externalConfiguration.getSuperUser();
    String keytabFile = externalConfiguration.getKeytab();
    return new KerberosProperties(kdc, realm, technicalUser, principal, keytabFile);
  }
}
