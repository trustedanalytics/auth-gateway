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
package org.trustedanalytics.auth.gateway.yarn.cloudera.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.auth.gateway.yarn.PoolConfiguration;
import org.trustedanalytics.auth.gateway.yarn.cloudera.ApiConnection;
import org.trustedanalytics.auth.gateway.yarn.cloudera.ConfigurationException;
import org.trustedanalytics.auth.gateway.yarn.cloudera.YarnScheduledAllocations;
import org.trustedanalytics.auth.gateway.yarn.cloudera.queues.Queue;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class YarnClouderaApiClientTest {

  private final static String TEST_ORG = "test_org";

  private final static String TEST_ORG_FROM_FILE = "test10";

  @InjectMocks
  private YarnClouderaApiClient yarnApiClient;

  @Mock
  private ApiConnection apiConnection;

  private YarnScheduledAllocations validConfiguration;

  @Before
  public void initialize() throws IOException {
    String validConfig = IOUtils.toString(getClass().getResourceAsStream("/validPoolConfig.json"));
    validConfiguration = new ObjectMapper().readValue(validConfig, YarnScheduledAllocations.class);
  }

  @Test
  public void addQueue_queueInConfiguration_poolConfigurationCreated()
      throws ConfigurationException {
    when(apiConnection.getConfiguration()).thenReturn(validConfiguration);

    Optional<PoolConfiguration> poolConfiguration = yarnApiClient.addQueue(TEST_ORG);
    List<Queue> queuesList =
        poolConfiguration.orElseThrow(
            () -> new IllegalStateException("Test failed, expected configuration is null."))
            .getQueuesList();

    assertThat(queuesList.size(), equalTo(validConfiguration.getQueuesList().size() + 1));
    assertThat(queuesList.stream().anyMatch(q -> q.getName().equals(TEST_ORG)), equalTo(true));
  }


  @Test
  public void addQueue_queueAlreadyExsits_doNotthrowConfigurationException()
      throws ConfigurationException {
    when(apiConnection.getConfiguration()).thenReturn(validConfiguration);

    Optional<PoolConfiguration> poolConfiguration = yarnApiClient.addQueue(TEST_ORG_FROM_FILE);
    assertThat(poolConfiguration.isPresent(), equalTo(false));
  }

  @Test(expected = ConfigurationException.class)
  public void addQueue_connectionException_throwConfigurationException()
      throws ConfigurationException {
    when(apiConnection.getConfiguration()).thenThrow(ConfigurationException.class);

    Optional<PoolConfiguration> poolConfiguration = yarnApiClient.addQueue(TEST_ORG);
  }

  @Test
  public void deleteQueue_queueNotInConfiguration_poolConfigurationCreated()
      throws ConfigurationException {
    when(apiConnection.getConfiguration()).thenReturn(validConfiguration);

    Optional<PoolConfiguration> poolConfiguration = yarnApiClient.deleteQueue(TEST_ORG_FROM_FILE);
    List<Queue> queuesList =
        poolConfiguration.orElseThrow(
            () -> new IllegalStateException("Test failed, expected configuration is null."))
            .getQueuesList();

    assertThat(queuesList.size(), equalTo(validConfiguration.getQueuesList().size() - 1));
    assertThat(queuesList.stream().anyMatch(q -> q.getName().equals(TEST_ORG_FROM_FILE)),
        equalTo(false));
  }

  @Test(expected = ConfigurationException.class)
  public void deleteQueue_connectionException_throwConfigurationException()
      throws ConfigurationException {
    when(apiConnection.getConfiguration()).thenThrow(ConfigurationException.class);

    Optional<PoolConfiguration> poolConfiguration = yarnApiClient.deleteQueue(TEST_ORG_FROM_FILE);
  }

  @Test
  public void deleteQueue_queueNotExists_doNotthrowConfigurationException()
      throws ConfigurationException {
    when(apiConnection.getConfiguration()).thenReturn(validConfiguration);

    Optional<PoolConfiguration> poolConfiguration = yarnApiClient.deleteQueue(TEST_ORG);
    assertThat(poolConfiguration.isPresent(), equalTo(false));
  }
}
