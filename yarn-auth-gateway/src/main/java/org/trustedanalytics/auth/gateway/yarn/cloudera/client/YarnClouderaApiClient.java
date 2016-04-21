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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.auth.gateway.yarn.PoolConfiguration;
import org.trustedanalytics.auth.gateway.yarn.YarnApiClient;
import org.trustedanalytics.auth.gateway.yarn.cloudera.ApiConnection;
import org.trustedanalytics.auth.gateway.yarn.cloudera.ConfigurationException;
import org.trustedanalytics.auth.gateway.yarn.cloudera.YarnScheduledAllocations;
import org.trustedanalytics.auth.gateway.yarn.cloudera.queues.Queue;
import org.trustedanalytics.auth.gateway.yarn.cloudera.queues.SchedulablePropertiesList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Profile("yarn-auth-gateway")
@Configuration
public class YarnClouderaApiClient implements YarnApiClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(YarnApiClient.class);

  private static final String SCHEDULER_TYPE = "fair";

  private static final String ACL_TYPE = "*";

  @Autowired
  private ApiConnection apiConnection;

  @Override
  public synchronized Optional<PoolConfiguration> addQueue(String queue) throws ConfigurationException {
    YarnScheduledAllocations newConfiguration = null;
    YarnScheduledAllocations configuration = apiConnection.getConfiguration();
    List<Queue> queuesList = new ArrayList<>(configuration.getQueuesList());
    if (queuesList.stream().noneMatch(q -> q.getName().equals(queue))) {
      List<SchedulablePropertiesList> propertiesList =
          Arrays.asList(SchedulablePropertiesList.createInstance("default", 1));
      Queue newQueue =
          Queue.queueBuilder().name(queue).schedulingPolicy(SCHEDULER_TYPE)
              .aclSubmitApps(String.format("%s %s", ACL_TYPE, queue)).aclAdministerApps(ACL_TYPE)
              .schedulablePropertiesList(propertiesList).build();
      queuesList.add(newQueue);
      newConfiguration =
          YarnScheduledAllocations.createInstance(configuration,
              Arrays.asList(Queue.createInstance(configuration.getRootQueue(), queuesList)));

      apiConnection.updateConfiguration(getStringRepresentation(newConfiguration));
    } else {
      LOGGER.warn(String.format("Queue already exsits: %s", queue));
    }
    return Optional.ofNullable(newConfiguration);
  }

  @Override
  public synchronized Optional<PoolConfiguration> deleteQueue(String queue) throws ConfigurationException {
    YarnScheduledAllocations newConfiguration = null;
    YarnScheduledAllocations configuration = apiConnection.getConfiguration();
    List<Queue> queuesList = new ArrayList<>(configuration.getQueuesList());
    if (queuesList.stream().anyMatch(q -> q.getName().equals(queue))) {
      queuesList.removeIf(q -> q.getName().equals(queue));

      newConfiguration =
          YarnScheduledAllocations.createInstance(configuration,
              Arrays.asList(Queue.createInstance(configuration.getRootQueue(), queuesList)));

      apiConnection.updateConfiguration(getStringRepresentation(newConfiguration));
    } else {
      LOGGER.warn(String.format("Queue not exsits: %s", queue));
    }
    return Optional.ofNullable(newConfiguration);
  }

  private String getStringRepresentation(YarnScheduledAllocations yarnScheduledAllocations)
      throws ConfigurationException {
    try {
      return new ObjectMapper().writeValueAsString(yarnScheduledAllocations);
    } catch (JsonProcessingException e) {
      throw new ConfigurationException("ServiceConfiguration malformed", e);
    }
  }
}
