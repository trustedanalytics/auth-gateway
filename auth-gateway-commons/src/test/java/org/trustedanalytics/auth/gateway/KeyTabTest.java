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

import org.apache.hadoop.conf.Configuration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeyTabTest {

  @Mock
  SystemEnvironment systemEnvironment;

  private String expectedKeyTabPath = "/tmp/jojo.keytab";

  @Before
  public void setUp() throws IOException {
    Configuration hadoopConf = new Configuration(false);
    hadoopConf.set(KeyTab.KRB_PRINC_TO_SYS_USER_NAME_RULES, "DEFAULT");
    when(systemEnvironment.getHadoopConfiguration()).thenReturn(hadoopConf);
    when(systemEnvironment.getVariable("KRB_KDC")).thenReturn("localhost");
    when(systemEnvironment.getVariable("KRB_REALM")).thenReturn("JOJOREALM");
  }

  @After
  public void tearDown() throws Exception {
    new File(expectedKeyTabPath).delete();
  }

  @Test
  public void testGetFullKeyTabFilePath_InitializedKeyTab_returnPathToCreatedKeyTab()
      throws Exception {
    //when
    KeyTab toTest = KeyTab.createInstance("", "jojo/sys", systemEnvironment);

    //then
    String expectedKeyTabPath = "/tmp/jojo.keytab";
    Assert.assertEquals(expectedKeyTabPath, toTest.getFullKeyTabFilePath());
    Assert.assertTrue(new File(expectedKeyTabPath).exists());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInstance_incorrectBase64Scheme_throwsException() throws Exception {
    //when
    KeyTab toTest = KeyTab.createInstance("=-a=-asda=-=-", "jojo/sys", systemEnvironment);

    //then
    //throws exception
  }

}
