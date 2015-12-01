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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.hadoop.conf.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LoggedInKerberosClientTest {

  private LoggedInKerberosClient kerberosClient;

  @Mock
  private Configuration configuration;

  private KerberosProperties kerberosProperties = new KerberosProperties("test", "test", "test_cf",
      "test_hdfs", new byte[] {});

  private File baseDir;

  @Before
  public void init() throws Exception {
    baseDir = com.google.common.io.Files.createTempDir();
    baseDir.deleteOnExit();
  }

  @Test
  public void createInstance_keyTabFileNotExists_fileCreated() throws IOException {
    kerberosClient =
        new LoggedInKerberosClient(kerberosProperties, baseDir.getAbsolutePath() + "/super.keytab");
    assertThat(Files.exists(Paths.get(baseDir.getAbsolutePath() + "/super.keytab")), equalTo(true));
  }

  @Test
  public void createInstance_underKeytabFilePathFileExists_doNothing() throws IOException {
    Path path = Paths.get(baseDir.getAbsolutePath() + "/super.keytab");
    Files.createFile(path);
    Files.write(path, new byte[]{});
    kerberosClient =
        new LoggedInKerberosClient(kerberosProperties, baseDir.getAbsolutePath() + "/super.keytab");
  }

  @Test(expected = IOException.class)
  public void createInstance_underKeytabFilePathDirectoryExists_throwIOException()
      throws IOException {
    Files.createDirectory(Paths.get(baseDir.getAbsolutePath() + "/super.keytab"));
    kerberosClient = new LoggedInKerberosClient(kerberosProperties, baseDir.getAbsolutePath() + "/super.keytab");
  }
}
