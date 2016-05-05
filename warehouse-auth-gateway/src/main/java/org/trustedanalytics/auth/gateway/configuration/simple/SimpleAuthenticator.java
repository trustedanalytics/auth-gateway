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
package org.trustedanalytics.auth.gateway.configuration.simple;

import java.io.IOException;

import javax.security.auth.login.LoginException;

import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.trustedanalytics.auth.gateway.configuration.Authenticator;

import org.trustedanalytics.auth.gateway.utils.Qualifiers;
import sun.security.krb5.KrbException;

@Component
@Profile(Qualifiers.SIMPLE)
public class SimpleAuthenticator implements Authenticator {

  @Autowired
  private WarehouseClientConfiguration config;

  @Override
  public UserGroupInformation getUserUGI() throws LoginException, IOException, KrbException {
    return UserGroupInformation.getBestUGI(null, config.getSuperUser());
  }

  @Override
  public String getRealm() {
    return "";
  }

  @Override
  public String getSuperUser() throws IOException {
    return config.getSuperUser();
  }
}
