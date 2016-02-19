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

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.auth.gateway.hdfs.utils.Qualifiers;

@Profile(Qualifiers.HDFS)
@Configuration
public class ExternalConfiguration {

  @Value("${hdfs.clientKeytab}")
  private String keytab;

  @Value("${hdfs.superUser}")
  @NotNull
  private String superUser;

  @Value("${hdfs.hiveUser}")
  @NotNull
  private String hiveUser;

  @Value("${hdfs.vcapUser}")
  @NotNull
  private String vcapUser;

  @Value("${hdfs.arcadiaUser}")
  @NotNull
  private String arcadiaUser;

  public String getKeytab() {
    return keytab;
  }

  public void setKeytab(String keytab) {
    this.keytab = keytab;
  }

  public String getSuperUser() {
    return superUser;
  }

  public void setSuperUser(String superUser) {
    this.superUser = superUser;
  }

  public String getHiveUser() {
    return hiveUser;
  }

  public void setHiveUser(String hiveUser) {
    this.hiveUser = hiveUser;
  }

  public String getArcadiaUser() {
    return arcadiaUser;
  }

  public void setArcadiaUser(String arcadiaUser) {
    this.arcadiaUser = arcadiaUser;
  }

  public String getVcapUser() {
    return vcapUser;
  }

  public void setVcapUser(String vcapUser) {
    this.vcapUser = vcapUser;
  }
}
