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
package org.trustedanalytics.auth.gateway.yarn.cloudera;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.github.rholder.retry.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.auth.gateway.yarn.cloudera.client.api.ApiEndpoints;
import org.trustedanalytics.auth.gateway.yarn.cloudera.client.api.entity.*;
import org.trustedanalytics.auth.gateway.yarn.cloudera.config.ClouderaConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicates;

@Profile("yarn-auth-gateway")
@Configuration
public class ApiConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiConnection.class);

  private static final String YARN_SCHEDULED_ALLOCATIONS = "yarn_fs_scheduled_allocations";

  private static final String YARN_SERVICE_TYPE = "YARN";

  @Autowired
  private ClouderaConfiguration configuration;

  public YarnScheduledAllocations getConfiguration() throws ConfigurationException {
    List<ServiceConfiguration> serviceConfigurationList =
        createRestTemplate().getForObject(getClouderaUrl(ApiEndpoints.CONFIGURATION),
            ApiServiceConfiguration.class, getCluster().getName(), YARN_SERVICE_TYPE).getItems();

    ServiceConfiguration serviceConfiguration =
        serviceConfigurationList.stream()
            .filter(conf -> conf.getName().equals(YARN_SCHEDULED_ALLOCATIONS)).findAny()
            .orElseThrow(() -> new ConfigurationException("ServiceConfiguration malformed"));
    try {
      return new ObjectMapper().readValue(serviceConfiguration.getValue(),
          YarnScheduledAllocations.class);
    } catch (IOException e) {
      throw new ConfigurationException("ServiceConfiguration malformed", e);
    }
  }

  public void updateConfiguration(String queueConfiguration) throws ConfigurationException {
    ApiServiceConfiguration serviceConfiguration =
        new ApiServiceConfiguration(Arrays.asList(new ServiceConfiguration(
            YARN_SCHEDULED_ALLOCATIONS, queueConfiguration)));

    RestTemplate restTemplate = createRestTemplate();
    String name = getCluster().getName();
    restTemplate.put(getClouderaUrl(ApiEndpoints.CONFIGURATION), serviceConfiguration, name,
        YARN_SERVICE_TYPE);
    Command command =
        restTemplate.postForObject(getClouderaUrl(ApiEndpoints.POOLS_REFRESH), null, Command.class,
            name);
    updateConfigurationWithRetries(command, restTemplate, name);
  }

  private RestTemplate createRestTemplate() {
    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials(configuration.getUser(), configuration.getPassword()));
    HttpClient httpClient =
        HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();

    return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
  }

  private void updateConfigurationWithRetries(Command command, RestTemplate restTemplate, String cluster)
      throws ConfigurationException {
    Callable<Boolean> callable = new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        LOGGER.info("Waiting for cloudera update command finish");
        ApiCommand apiCommand =
          restTemplate.getForObject(getClouderaUrl(ApiEndpoints.COMMANDS), ApiCommand.class,
              cluster);
        return apiCommand.getItems().stream().noneMatch(c -> c.getId().compareTo(command.getId()) == 0);
      }
    };

    Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
        .retryIfResult(Predicates.equalTo(false))
        .retryIfExceptionOfType(IOException.class)
        .retryIfException()
        .withWaitStrategy(WaitStrategies.incrementingWait(5, TimeUnit.SECONDS, -1, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterAttempt(4))
        .build();

    try {
      retryer.call(callable);
    } catch (ExecutionException | RetryException e) {
      throw new ConfigurationException("ServiceConfiguration malformed, cannot update Configuration");
    }
  }

  private Cluster getCluster() {
    return createRestTemplate()
        .getForObject(getClouderaUrl(ApiEndpoints.CLUSTERS), ApiCluster.class).getItems().get(0);
  }

  private String getClouderaUrl(String path) {
    return String.format("http://%s:%s%s", configuration.getHost(), configuration.getPort(), path);
  }

}
