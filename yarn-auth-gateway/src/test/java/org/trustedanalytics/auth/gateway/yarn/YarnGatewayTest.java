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
package org.trustedanalytics.auth.gateway.yarn;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;
import org.trustedanalytics.auth.gateway.yarn.cloudera.ConfigurationException;

@RunWith(MockitoJUnitRunner.class)
public class YarnGatewayTest {

  private final static String TEST_ORG = "test_org";

  @InjectMocks
  private YarnGateway yarnGateway;

  @Mock
  private YarnApiClient yarnApiClient;

  @Test
  public void addOrganization_yarnApiClientCalled_creationSuccess()
      throws AuthorizableGatewayException, IOException, ConfigurationException {
    yarnGateway.addOrganization(TEST_ORG);
    verify(yarnApiClient).addQueue(TEST_ORG);
  }

  @Test(expected = AuthorizableGatewayException.class)
  public void addOrganization_throwConfigurationException_creationFailed()
      throws AuthorizableGatewayException, IOException, ConfigurationException {
    when(yarnApiClient.addQueue(TEST_ORG)).thenThrow(ConfigurationException.class);
    yarnGateway.addOrganization(TEST_ORG);
  }

  @Test(expected = AuthorizableGatewayException.class)
  public void addOrganization_throwException_creationFailed() throws AuthorizableGatewayException,
      IOException, ConfigurationException {
    when(yarnApiClient.addQueue(TEST_ORG)).thenThrow(Exception.class);
    yarnGateway.addOrganization(TEST_ORG);
  }

  @Test
  public void removeOrganization_yarnApiClientCalled_deletionSuccess()
      throws AuthorizableGatewayException, IOException, ConfigurationException {
    yarnGateway.removeOrganization(TEST_ORG);
    verify(yarnApiClient).deleteQueue(TEST_ORG);
  }

  @Test(expected = AuthorizableGatewayException.class)
  public void removeOrganization_throwConfigurationException_creationFailed()
      throws AuthorizableGatewayException, IOException, ConfigurationException {
    when(yarnApiClient.deleteQueue(TEST_ORG)).thenThrow(ConfigurationException.class);
    yarnGateway.removeOrganization(TEST_ORG);
  }

  @Test(expected = AuthorizableGatewayException.class)
  public void removeOrganization_throwException_creationFailed()
      throws AuthorizableGatewayException, IOException, ConfigurationException {
    when(yarnApiClient.deleteQueue(TEST_ORG)).thenThrow(Exception.class);
    yarnGateway.removeOrganization(TEST_ORG);
  }

}
