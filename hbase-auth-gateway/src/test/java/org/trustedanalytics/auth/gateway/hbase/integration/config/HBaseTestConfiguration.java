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

package org.trustedanalytics.auth.gateway.hbase.integration.config;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.client.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.trustedanalytics.auth.gateway.hbase.kerberos.KerberosHbaseProperties;

@ActiveProfiles("test")
@Configuration
public class HBaseTestConfiguration {

  @Autowired
  private HBaseTestingUtility utility;

  @Bean
  public KerberosHbaseProperties getKerberosProperties() throws IOException {
    return new KerberosHbaseProperties("kdc", "krealm");
  }

  @Bean
  public Connection getHBaseConnection()
      throws IOException, InterruptedException, URISyntaxException {
    return utility.getConnection();
  }
}
