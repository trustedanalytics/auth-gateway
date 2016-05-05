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
package org.trustedanalytics.auth.gateway.configuration.kerberos;

import java.io.IOException;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.authentication.util.KerberosName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.trustedanalytics.auth.gateway.configuration.Authenticator;
import org.trustedanalytics.auth.gateway.utils.Qualifiers;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;

import sun.security.krb5.KrbException;


@Component
@Profile(Qualifiers.KERBEROS)
public class KrbAuthenticator implements Authenticator {

  @Autowired
  private WarehouseKrbClientConfiguration conf;

  @Override
  public UserGroupInformation getUserUGI() throws LoginException, IOException, KrbException {
    KrbLoginManager loginManager = KrbLoginManagerFactory.getInstance()
        .getKrbLoginManagerInstance(conf.getKdc(), conf.getRealm());
    Subject subject =
        loginManager.loginWithKeyTab(conf.getSimpleConfig().getSuperUser(), conf.getKeyTabPath());
    Configuration hadoopConf = new Configuration();
    hadoopConf.set("hadoop.security.authentication", "kerberos");
    loginManager.loginInHadoop(subject, hadoopConf);
    return loginManager.getUGI(subject);
  }

  @Override
  public String getSuperUser() throws IOException {
    KerberosName princName =
        new KerberosName(conf.getSimpleConfig().getSuperUser().concat("@").concat(conf.getRealm()));
    return princName.getShortName();
  }

  @Override
  public String getRealm() {
    return conf.getRealm();
  }
}
