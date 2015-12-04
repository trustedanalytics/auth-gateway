/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.auth.gateway;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;

import sun.security.krb5.KrbException;

import java.io.IOException;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

@Component
@Profile("sentry-auth-gateway")
public class KrbSentryAuthenticator implements SentryAuthenticator {

  @Autowired
  private KrbClientConfiguration conf;

  @Override
  public UserGroupInformation sentryUserUGI() throws LoginException,
                                                     IOException,
                                                     KrbException {
    KrbLoginManager loginManager =
        KrbLoginManagerFactory.getInstance().getKrbLoginManagerInstance(conf.getKdc(),
                                                                        conf.getRealm());
    Subject subject = loginManager.loginWithKeyTab(conf.getSuperUser(), conf.getKeyTabPath());
    Configuration conf = new Configuration();
    conf.set("hadoop.security.authentication", "kerberos");
    loginManager.loginInHadoop(subject, conf);
    return loginManager.getUGI(subject);
  }

  public String getSuperUser() {
    return conf.getSuperUser();
  }

  public String getRealm() {
    return conf.getRealm();
  }

}