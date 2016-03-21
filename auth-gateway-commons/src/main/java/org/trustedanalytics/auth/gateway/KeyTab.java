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

import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;

import com.google.common.annotations.VisibleForTesting;
import org.apache.hadoop.security.authentication.util.KerberosName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

public final class KeyTab {

  private KerberosName krbPrincName;

  static final String KRB_PRINC_TO_SYS_USER_NAME_RULES = "hadoop.security.auth_to_local";

  static final String KRB_CONF_SERVICE_NAME = "kerberos-service";

  private static final String KEY_TABS_DIR = "/tmp/";

  private static final String KEY_TAB_FILE_POSTFIX = ".keytab";

  private static final String NAME_REALM_SEPARATOR_STR = "@";

  private KeyTab(String keytab, String userName, SystemEnvironment systemEnvironment)
      throws IOException {

    String realm = systemEnvironment.getVariable(SystemEnvironment.KRB_REALM);
    String kdc = systemEnvironment.getVariable(SystemEnvironment.KRB_KDC);
    KrbLoginManagerFactory.getInstance().getKrbLoginManagerInstance(kdc, realm);

    if (!KerberosName.hasRulesBeenSet()) {
      KerberosName.setRules(
          systemEnvironment.getHadoopConfiguration().get(KRB_PRINC_TO_SYS_USER_NAME_RULES));
    }
    krbPrincName = new KerberosName(userName.concat(NAME_REALM_SEPARATOR_STR).concat(realm));
    saveKeytabIfNotExists(keytab);
  }

  public static KeyTab createInstance(String keyTab, String userName) throws IOException {
    return new KeyTab(keyTab, userName, new SystemEnvironment());
  }

  @VisibleForTesting
  static KeyTab createInstance(String keyTab, String userName, SystemEnvironment systemEnvironment)
      throws IOException {
    return new KeyTab(keyTab, userName, systemEnvironment);
  }

  public String getFullKeyTabFilePath() throws IOException {
    return KEY_TABS_DIR.concat(krbPrincName.getShortName()).concat(KEY_TAB_FILE_POSTFIX);
  }

  private void saveKeytabIfNotExists(String serializedKeyTab) throws IOException {
    String keyTabFilePath = getFullKeyTabFilePath();
    Path path = Paths.get(keyTabFilePath);
    if (Files.notExists(path)) {
      Files.write(path, Base64.getDecoder().decode(serializedKeyTab),
          StandardOpenOption.CREATE_NEW);
    } else if (Files.isDirectory(path)) {
      throw new IOException(
          String.format("Under path %s exists directory. It's path where hdfs superuser keytab "
              + "is stored. Please move or delete this directory", keyTabFilePath));
    }
  }
}
