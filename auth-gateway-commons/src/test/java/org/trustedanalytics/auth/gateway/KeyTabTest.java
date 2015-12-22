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
import org.trustedanalytics.hadoop.config.client.AppConfiguration;
import org.trustedanalytics.hadoop.config.client.Property;
import org.trustedanalytics.hadoop.config.client.ServiceInstanceConfiguration;
import org.trustedanalytics.hadoop.config.client.ServiceType;

import java.io.File;
import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeyTabTest {

  @Mock
  AppConfiguration appConf;

  @Mock
  ServiceInstanceConfiguration hdfsConf;

  @Mock
  ServiceInstanceConfiguration krbConf;

  private String expectedKeyTabPath = "/tmp/jojo.keytab";

  @Before
  public void setUp() {
    Configuration hadoopConf = new Configuration(false);
    hadoopConf.set(KeyTab.KRB_PRINC_TO_SYS_USER_NAME_RULES, "DEFAULT");
    when(appConf.getServiceConfig(ServiceType.HDFS_TYPE)).thenReturn(hdfsConf);
    when(appConf.getServiceConfig(KeyTab.KRB_CONF_SERVICE_NAME)).thenReturn(krbConf);
    when(hdfsConf.asHadoopConfiguration()).thenReturn(hadoopConf);
    when(krbConf.getProperty(Property.KRB_KDC)).thenReturn(Optional.of("localhost"));
    when(krbConf.getProperty(Property.KRB_REALM)).thenReturn(Optional.of("JOJOREALM"));
  }


  @After
  public void tearDown() throws Exception {
    new File(expectedKeyTabPath).delete();
  }

  @Test
  public void testGetFullKeyTabFilePath_InitializedKeyTab_returnPathToCreatedKeyTab()
      throws Exception {
    //when
    KeyTab toTest = KeyTab.createInstance("", "jojo/sys", appConf);

    //then
    String expectedKeyTabPath = "/tmp/jojo.keytab";
    Assert.assertEquals(expectedKeyTabPath, toTest.getFullKeyTabFilePath());
    Assert.assertTrue(new File(expectedKeyTabPath).exists());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInstance_incorrectBase64Scheme_throwsException() throws Exception {
    //when
    KeyTab toTest = KeyTab.createInstance("=-a=-asda=-=-", "jojo/sys", appConf);

    //then
    //throws exception
  }

}