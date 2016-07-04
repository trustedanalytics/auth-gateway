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
package org.trustedanalytics.auth.gateway.cloud;

import org.trustedanalytics.auth.gateway.cloud.api.ApiResources;
import org.trustedanalytics.auth.gateway.cloud.api.ApiResponse;
import org.trustedanalytics.auth.gateway.engine.response.OrganizationState;
import org.trustedanalytics.auth.gateway.engine.response.UserState;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

import java.util.ArrayList;
import java.util.List;

public class Cloud {

    private CloudApi api;

    public Cloud(CloudApi api) {
        this.api = api;
    }

    public List<OrganizationState> getOrganizations() throws AuthorizableGatewayException {
        List<OrganizationState> organizations = new ArrayList<>();

        getResourcesFromEndpoint((page) -> api.getOrganizations(page)).stream().forEach((resources) -> {
            OrganizationState organization = new OrganizationState(resources.getEntity().getName(),
                    resources.getMetadata().getGuid());
            organization.getUsers().addAll(getUsersForOrganization(organization));
            organizations.add(organization);
        });

        return organizations;
    }

    public List<UserState> getUsersForOrganization(OrganizationState organization) {
        List<UserState> users = new ArrayList<>();

        getResourcesFromEndpoint((page) -> api.getOrganizationUsers(organization.getGuid(), page)).stream()
                .forEach((resources) ->
                        users.add(new UserState(resources.getEntity().getName(), resources.getMetadata().getGuid())));

        return users;
    }

    private List<ApiResources> getResourcesFromEndpoint(CloudApiRequest request) {
        return getResourcesFromEndpoint(request, 1);
    }

    private List<ApiResources> getResourcesFromEndpoint(CloudApiRequest request, int page) {
        ApiResponse response = request.getResponse(page);
        List<ApiResources> resources = response.getResources();
        if (response.getNextUrl() != null)
            resources.addAll(getResourcesFromEndpoint(request, page + 1));

        return resources;
    }
}
