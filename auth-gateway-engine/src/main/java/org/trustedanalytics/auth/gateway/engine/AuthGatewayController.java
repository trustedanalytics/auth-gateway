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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.trustedanalytics.auth.gateway.engine.async.AsyncComponent;
import org.trustedanalytics.auth.gateway.engine.async.AsyncRequestState;
import org.trustedanalytics.auth.gateway.engine.async.NoContentException;
import org.trustedanalytics.auth.gateway.engine.async.TaskNotFinishedException;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

import java.util.UUID;

@RestController
class AuthGatewayController {

  private final Engine authGatewayEngine;
  private final AsyncComponent asyncComponent;

  @Autowired
  public AuthGatewayController(Engine authGatewayEngine, AsyncComponent asyncComponent) {
    this.authGatewayEngine = authGatewayEngine;
    this.asyncComponent = asyncComponent;
  }

  @ApiOperation("Creating organization: in case of hdfs - creating directory, zookeeper - creating znode, " +
          "sentry - roles on cdh cluster, hgm - usergroupmapping")
  @RequestMapping(value = "/organizations/{orgId}", method = RequestMethod.PUT)
  public AuthgatewayResponse addOrganization(@PathVariable String orgId,
                                @RequestParam(value = "async", defaultValue = "false") Boolean async)
          throws AuthorizableGatewayException {
    return callRequest(() -> authGatewayEngine.addOrganization(orgId), async);
  }

  @ApiOperation("Adding user's access to hadoop components in given organization")
  @RequestMapping(value = "/organizations/{orgId}/users/{userId}", method = RequestMethod.PUT)
  public AuthgatewayResponse addUserToOrganization(@PathVariable String userId, @PathVariable String orgId,
                                      @RequestParam(value = "async", defaultValue = "false") Boolean async)
          throws AuthorizableGatewayException {
    return callRequest(() -> authGatewayEngine.addUserToOrg(userId, orgId), async);
  }

  @ApiOperation("Removing organization: in case of hdfs - removing directory, zookeeper - removing znode, " +
          "sentry - roles on cdh cluster, hgm - usergroupmapping")
  @RequestMapping(value = "/organizations/{orgId}", method = RequestMethod.DELETE)
  public AuthgatewayResponse deleteOrganization(@PathVariable String orgId,
                                   @RequestParam(value = "async", defaultValue = "false") Boolean async)
          throws AuthorizableGatewayException {
    return callRequest(() -> {
      authGatewayEngine.removeOrganization(orgId);
      return null;
    }, async);
  }

  @ApiOperation("Removing user's access to hadoop components in given organization")
  @RequestMapping(value = "/organizations/{orgId}/users/{userId}", method = RequestMethod.DELETE)
  public AuthgatewayResponse deleteUserFromOrganization(@PathVariable String userId, @PathVariable String orgId,
                                           @RequestParam(value = "async", defaultValue = "false") Boolean async)
          throws AuthorizableGatewayException {
    return callRequest(() -> {
      authGatewayEngine.removeUserFromOrg(userId, orgId);
      return null;
    }, async);
  }

  @ApiOperation("Synchronizing CF organizations and users with CDH")
  @RequestMapping(value = "/synchronize", method = RequestMethod.PUT)
  public AuthgatewayResponse synchronize(@RequestParam(value = "async", defaultValue = "false") Boolean async)
          throws AuthorizableGatewayException {
    return callRequest(() -> authGatewayEngine.synchronize(), async);
  }

  @ApiOperation("Synchronizing CF organization with CDH")
  @RequestMapping(value = "/synchronize/organizations/{orgId}", method = RequestMethod.PUT)
  public AuthgatewayResponse synchronizeOrg(@PathVariable String orgId,
                               @RequestParam(value = "async", defaultValue = "false") Boolean async)
          throws AuthorizableGatewayException {
    return callRequest(() -> authGatewayEngine.synchronizeOrg(orgId), async);
  }

  @ApiOperation("Synchronizing CF user in organization with CDH")
  @RequestMapping(value = "/synchronize/organizations/{orgId}/users/{userId}", method = RequestMethod.PUT)
  public AuthgatewayResponse synchronizeUser(@PathVariable String orgId, @PathVariable String userId,
                                @RequestParam(value = "async", defaultValue = "false") Boolean async)
          throws AuthorizableGatewayException {
    return callRequest(() -> authGatewayEngine.synchronizeUser(orgId, userId), async);
  }

  @ApiOperation("Get all organizations state")
  @RequestMapping(value = "/state", method = RequestMethod.GET)
  public AuthgatewayResponse state(@RequestParam(value = "async", defaultValue = "false") Boolean async)
          throws AuthorizableGatewayException {
    return callRequest(() -> authGatewayEngine.state(), async);
  }

  @ApiOperation("Get organization state")
  @RequestMapping(value = "/state/organizations/{orgId}", method = RequestMethod.GET)
  public AuthgatewayResponse state(@PathVariable String orgId,
                      @RequestParam(value = "async", defaultValue = "false") Boolean async)
          throws AuthorizableGatewayException {
    return callRequest(() -> authGatewayEngine.orgState(orgId), async);
  }

  @ApiOperation("Get user state")
  @RequestMapping(value = "/state/organizations/{orgId}/users/{userId}", method = RequestMethod.GET)
  public AuthgatewayResponse state(@PathVariable String orgId, @PathVariable String userId,
                      @RequestParam(value = "async", defaultValue = "false") Boolean async)
          throws AuthorizableGatewayException {
    return callRequest(() -> authGatewayEngine.userState(orgId, userId), async);
  }

  @ApiOperation("Get async task state")
  @RequestMapping(value = "/jobs/{uuid}", method = RequestMethod.GET)
  public AuthgatewayResponse async(@PathVariable String uuid) throws AuthorizableGatewayException {
    return (AuthgatewayResponse) asyncComponent.getRequestState(UUID.fromString(uuid)).getResult();
  }

  @ExceptionHandler(TaskNotFinishedException.class)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public AsyncRequestState parseNotFinishedState(TaskNotFinishedException e) {
    return e.getState();
  }

  @ExceptionHandler(NoContentException.class)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void parseNoContentState(NoContentException e) { }

  private AuthgatewayResponse callRequest(RequestApplier request, Boolean async) throws AuthorizableGatewayException {
    if (!async) {
      return request.parseRequest();
    }
    else {
      return asyncComponent.addTask(request);
    }
  }
}
