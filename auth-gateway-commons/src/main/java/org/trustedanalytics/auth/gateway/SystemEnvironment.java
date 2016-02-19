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

import java.io.IOException;
import java.util.Optional;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.cfbroker.config.HadoopZipConfiguration;

public class SystemEnvironment {

  public static final String KRB_KDC = "KRB_KDC";
  public static final String KRB_REALM = "KRB_REALM";
  public static final String KRB_USER = "KRB_USER";
  public static final String KRB_PASSWORD = "KRB_PASSWORD";

  private static final Logger LOGGER = LoggerFactory.getLogger(SystemEnvironment.class);
  private static final String HADOOP_PROVIDED_ZIP = "HADOOP_PROVIDED_ZIP";

  public String getVariable(String varName) {
    return Optional.ofNullable(System.getenv(varName)).orElseGet(() -> {
      String errorMsg = getErrorMsg(varName);
      LOGGER.error(errorMsg);
      throw new NullPointerException(errorMsg);
    });
  }

  public Configuration getHadoopConfiguration() throws IOException {
    String encodedZip = getVariable(HADOOP_PROVIDED_ZIP);
    return HadoopZipConfiguration.createHadoopZipConfiguration(encodedZip)
        .getAsHadoopConfiguration();
  }

  private static String getErrorMsg(String varName) {
    return varName + " not found in ENVIRONMENT";
  }
}
