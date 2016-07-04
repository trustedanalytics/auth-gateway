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
package org.trustedanalytics.auth.gateway.engine.integration.tests;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.auth.gateway.cloud.Cloud;
import org.trustedanalytics.auth.gateway.cloud.CloudApi;
import org.trustedanalytics.auth.gateway.cloud.api.ApiEntity;
import org.trustedanalytics.auth.gateway.cloud.api.ApiMetadata;
import org.trustedanalytics.auth.gateway.cloud.api.ApiResources;
import org.trustedanalytics.auth.gateway.cloud.api.ApiResponse;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Profile("test")
public class CloudConfig {

  private CloudApi cloudApi;

  private ApiResources createResource(String id)
  {
    ApiResources resources = new ApiResources();
    ApiEntity entity = new ApiEntity();
    entity.setUsername(id);
    ApiMetadata metadata = new ApiMetadata();
    metadata.setGuid(id);

    resources.setMetadata(metadata);
    resources.setEntity(entity);

    return resources;
  }

  private ApiResponse getResponse(String id, String id2) {
    ApiResponse response = new ApiResponse();
    response.setNextUrl(null);

    response.setResources(Arrays.asList(createResource(id), createResource(id2)));

    return response;
  }

  @Bean
  public Cloud getCloud() {
    cloudApi = mock(CloudApi.class);

    when(cloudApi.getOrganizations(1)).thenReturn(getResponse(AuthGatewayControllerTest.ORG_ID,
            AuthGatewayControllerTest.ORG1_ID));
    when(cloudApi.getOrganizationUsers(AuthGatewayControllerTest.ORG_ID, 1))
            .thenReturn(getResponse(AuthGatewayControllerTest.USER_ID, AuthGatewayControllerTest.USER1_ID));
    when(cloudApi.getOrganizationUsers(AuthGatewayControllerTest.ORG1_ID, 1))
            .thenReturn(getResponse(AuthGatewayControllerTest.USER_ID, AuthGatewayControllerTest.USER1_ID));

    return new Cloud(cloudApi);
  }

}
