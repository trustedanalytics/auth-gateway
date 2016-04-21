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

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

@RestController
class AuthGatewayController {

    private final Engine authGatewayEngine;

    @Autowired
    public AuthGatewayController(Engine authGatewayEngine) {
        this.authGatewayEngine = authGatewayEngine;
    }

    @RequestMapping(value = "/users/{userId}", method = RequestMethod.PUT)
    public void addUser(@PathVariable String userId)
            throws AuthorizableGatewayException {
        authGatewayEngine.addUser(userId);
    }

    @ApiOperation("Creating organization: in case of hdfs - creating directory, zookeeper - creating znode, " +
            "sentry - roles on cdh cluster, hgm - usergroupmapping")
    @RequestMapping(value = "/organizations/{orgId}", method = RequestMethod.PUT)
    public void addOrganization(@PathVariable String orgId)
            throws AuthorizableGatewayException {
        authGatewayEngine.addOrganization(orgId);
    }

    @ApiOperation("Adding user's access to hadoop components in given organization")
    @RequestMapping(value = "/organizations/{orgId}/users/{userId}", method = RequestMethod.PUT)
    public void addUserToOrganization(@PathVariable String userId, @PathVariable String orgId)
            throws AuthorizableGatewayException {
        authGatewayEngine.addUserToOrg(userId, orgId);
    }

    @RequestMapping(value = "/users/{userId}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable String userId)
            throws AuthorizableGatewayException {
        authGatewayEngine.removeUser(userId);
    }

    @ApiOperation("Removing organization: in case of hdfs - removing directory, zookeeper - removing znode, " +
            "sentry - roles on cdh cluster, hgm - usergroupmapping")
    @RequestMapping(value = "/organizations/{orgId}", method = RequestMethod.DELETE)
    public void deleteOrganization(@PathVariable String orgId)
            throws AuthorizableGatewayException {
        authGatewayEngine.removeOrganization(orgId);
    }

    @ApiOperation("Removing user's access to hadoop components in given organization")
    @RequestMapping(value = "/organizations/{orgId}/users/{userId}", method = RequestMethod.DELETE)
    public void deleteUserFromOrganization(@PathVariable String userId, @PathVariable String orgId)
            throws AuthorizableGatewayException {
        authGatewayEngine.removeUserFromOrg(userId, orgId);
    }
}
