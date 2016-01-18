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

import org.trustedanalytics.auth.gateway.SystemEnvironment;
import org.trustedanalytics.auth.gateway.zookeeper.kerberos.KerberosProperties;

import java.io.IOException;

public class ZookeeperAuthorizationEnv {

  private static final String ZK_CLUSTER_URL = "ZK_CLUSTER_URL";

  public String zookeeperQuorum() {
    return new SystemEnvironment().getVariable(ZK_CLUSTER_URL);
  }

  public String zookeeperRootNode() {
    return "/org";
  }

  public KerberosProperties kerberosProperties() throws IOException {
    KerberosProperties krbProps = new KerberosProperties();
    SystemEnvironment systemEnvironment = new SystemEnvironment();
    krbProps.setKdc(systemEnvironment.getVariable(SystemEnvironment.KRB_KDC));
    krbProps.setRealm(systemEnvironment.getVariable(SystemEnvironment.KRB_REALM));
    krbProps.setUser(systemEnvironment.getVariable(SystemEnvironment.KRB_USER));
    krbProps.setPassword(systemEnvironment.getVariable(SystemEnvironment.KRB_PASSWORD));
    return krbProps;
  }
}
