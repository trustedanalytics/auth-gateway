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

package org.trustedanalytics.auth.gateway.zookeeper.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.auth.gateway.zookeeper.kerberos.KerberosProperties;
import org.trustedanalytics.hadoop.config.client.AppConfiguration;
import org.trustedanalytics.hadoop.config.client.Configurations;
import org.trustedanalytics.hadoop.config.client.Property;
import org.trustedanalytics.hadoop.config.client.ServiceInstanceConfiguration;
import org.trustedanalytics.hadoop.config.client.ServiceType;

import java.io.IOException;

public class ZookeeperAuthorizationEnv {

  private final static Logger LOGGER = LoggerFactory.getLogger(ZookeeperAuthorizationEnv.class);
  private final AppConfiguration appConf;

  public ZookeeperAuthorizationEnv() throws IOException {
    this.appConf = Configurations.newInstanceFromEnv();
  }

  public String zookeeperQuorum() {
    ServiceInstanceConfiguration zkConf = appConf.getServiceConfig(ServiceType.ZOOKEEPER_TYPE);
    return get(zkConf, Property.ZOOKEPER_URI, null);
  }

  public String zookeeperRootNode() {
    return "/org";
  }

  public KerberosProperties kerberosProperties() throws IOException {
    AppConfiguration appConfig = Configurations.newInstanceFromEnv();
    ServiceInstanceConfiguration krbConf = appConfig.getServiceConfig("kerberos-service");

    KerberosProperties krbProps = new KerberosProperties();
    krbProps.setKdc(get(krbConf, Property.KRB_KDC));
    krbProps.setRealm(get(krbConf, Property.KRB_REALM));
    krbProps.setUser(get(krbConf, Property.USER));
    krbProps.setPassword(get(krbConf, Property.PASSWORD));
    return krbProps;
  }

  private String get(ServiceInstanceConfiguration conf, Property property) {
    return get(conf, property, "");
  }

  private String get(ServiceInstanceConfiguration conf, Property property, String defaultValue) {
    return conf.getProperty(property).orElseGet(() -> {
      logErrorMsg(property);
      return defaultValue;
    });
  }

  private void logErrorMsg(Property property) {
    LOGGER.debug(getErrorMsg(property));
  }

  private String getErrorMsg(Property property) {
    return property.name() + " not found in VCAP_SERVICES";
  }
}
