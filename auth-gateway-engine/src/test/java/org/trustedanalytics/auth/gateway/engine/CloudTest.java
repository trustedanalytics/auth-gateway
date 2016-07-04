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
package org.trustedanalytics.auth.gateway.engine;

import org.junit.Before;
import org.junit.Test;
import org.trustedanalytics.auth.gateway.cloud.Cloud;
import org.trustedanalytics.auth.gateway.cloud.CloudApi;
import org.trustedanalytics.auth.gateway.cloud.api.ApiEntity;
import org.trustedanalytics.auth.gateway.cloud.api.ApiMetadata;
import org.trustedanalytics.auth.gateway.cloud.api.ApiResources;
import org.trustedanalytics.auth.gateway.cloud.api.ApiResponse;
import org.trustedanalytics.auth.gateway.engine.response.OrganizationState;
import org.trustedanalytics.auth.gateway.engine.response.UserState;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CloudTest {

    private static final String ORG_NAME = "org_name";

    private CloudApi cloudApi;

    private ApiResponse generateResponse(int size, boolean nextPage)
    {
        ApiResponse response = new ApiResponse();
        List<ApiResources> resources =new ArrayList<>();

        for(int x=0;x<size;x++)
        {
            ApiResources resource = new ApiResources();
            ApiMetadata metadata = new ApiMetadata();
            ApiEntity entity = new ApiEntity();

            metadata.setGuid(ORG_NAME);
            entity.setName(ORG_NAME);

            resource.setMetadata(metadata);
            resource.setEntity(entity);
            resources.add(resource);
        }
        response.setResources(resources);
        if(nextPage)
            response.setNextUrl("Some random url");

        return response;
    }

    @Before
    public void setUp() throws AuthorizableGatewayException {
        cloudApi = mock(CloudApi.class);
        doReturn(generateResponse(100, true)).when(cloudApi).getOrganizations(1);
        doReturn(generateResponse(100, true)).when(cloudApi).getOrganizations(2);
        doReturn(generateResponse(40, false)).when(cloudApi).getOrganizations(3);
    }

    @Test
    public void getOrganizations_listAllOrgs_verifySize() throws AuthorizableGatewayException
    {
        // given
        Cloud cloud = new Cloud(cloudApi);
        // when
        doReturn(generateResponse(0, false)).when(cloudApi).getOrganizationUsers(ORG_NAME, 1);
        List<OrganizationState> organizations = cloud.getOrganizations();

        // then
        verify(cloudApi).getOrganizations(1);
        verify(cloudApi).getOrganizations(2);
        verify(cloudApi).getOrganizations(3);
        verify(cloudApi, times(0)).getOrganizations(4);

        assertEquals(organizations.size(), 240);
    }

    @Test
    public void getUsers_listAllUsersForOrganization_verifySize() throws AuthorizableGatewayException
    {

        // given
        Cloud cloud = new Cloud(cloudApi);

        // when
        doReturn(generateResponse(100, true)).when(cloudApi).getOrganizationUsers(ORG_NAME, 1);
        doReturn(generateResponse(31, true)).when(cloudApi).getOrganizationUsers(ORG_NAME, 2);
        doReturn(generateResponse(40, false)).when(cloudApi).getOrganizationUsers(ORG_NAME, 3);

        List<UserState> users = cloud.getUsersForOrganization(new OrganizationState(ORG_NAME, ORG_NAME));

        // then
        verify(cloudApi).getOrganizationUsers(ORG_NAME, 1);
        verify(cloudApi).getOrganizationUsers(ORG_NAME, 2);
        verify(cloudApi).getOrganizationUsers(ORG_NAME, 3);
        verify(cloudApi, times(0)).getOrganizationUsers(ORG_NAME, 4);

        assertEquals(users.size(), 171);
    }
}
