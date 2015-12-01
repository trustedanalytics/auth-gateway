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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.trustedanalytics.auth.gateway.engine.Engine;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

@RestController
class AuthGatewayController {

    private final Engine authGatewayEngine;

    @Autowired
    public AuthGatewayController(Engine authGatewayEngine) {
        this.authGatewayEngine = authGatewayEngine;
    }

    @RequestMapping(value = "/users/{userId}", method = RequestMethod.PUT)
    public void addUser(@PathVariable String userId, @RequestParam String userName)
            throws AuthorizableGatewayException {
        authGatewayEngine.addUser(userId, userName);
    }

    @RequestMapping(value = "/organizations/{orgId}", method = RequestMethod.PUT)
    public void addOrganization(@PathVariable String orgId, @RequestParam String orgName)
            throws AuthorizableGatewayException {
        authGatewayEngine.addOrganization(orgId, orgName);
    }

    @RequestMapping(value = "/organizations/{orgId}/users/{userId}", method = RequestMethod.PUT)
    public void addUserToOrganization(@PathVariable String userId, @PathVariable String orgId)
            throws AuthorizableGatewayException {
        authGatewayEngine.addUserToOrg(userId, orgId);
    }

    @RequestMapping(value = "/users/{userId}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable String userId, @RequestParam String userName)
            throws AuthorizableGatewayException {
        authGatewayEngine.removeUser(userId, userName);
    }

    @RequestMapping(value = "/organizations/{orgId}", method = RequestMethod.DELETE)
    public void deleteOrganization(@PathVariable String orgId, @RequestParam String orgName)
            throws AuthorizableGatewayException {
        authGatewayEngine.removeOrganization(orgId, orgName);
    }

    @RequestMapping(value = "/organizations/{orgId}/users/{userId}", method = RequestMethod.DELETE)
    public void deleteUserFromOrganization(@PathVariable String userId, @PathVariable String orgId)
            throws AuthorizableGatewayException {
        authGatewayEngine.removeUserFromOrg(userId, orgId);
    }
}
