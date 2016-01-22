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
package org.trustedanalytics.auth.gateway.hgm;

import org.trustedanalytics.auth.gateway.hgm.entity.User;
import org.trustedanalytics.auth.gateway.hgm.utils.ApiEndpoints;
import org.trustedanalytics.auth.gateway.hgm.utils.Qualifiers;
import org.trustedanalytics.auth.gateway.spi.Authorizable;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Configuration
@Profile({Qualifiers.HTTPS, Qualifiers.KERBEROS})
public class HgmGateway implements Authorizable {

  private static final Logger LOGGER = LoggerFactory.getLogger(HgmGateway.class);

  private static final String ADMIN_POSTFIX = "_admin";

  private static final String NAME = "hgm";

  @Value("${group.mapping.url}")
  private String groupMappingServiceUrl;

  @Autowired
  private RestTemplate restTemplate;

  @Override
  public void addOrganization(String orgId) throws AuthorizableGatewayException {
    LOGGER.debug(String.format("Create organization %s", orgId));
    try {
      if (!getGroups().contains(orgId)) {
        restTemplate.postForObject(createUrl(ApiEndpoints.USERS),
            new User(orgId.concat(ADMIN_POSTFIX)), String.class, orgId);
      }
    } catch (RestClientException e) {
      throw new AuthorizableGatewayException(String.format("Can't add organization: %s", orgId), e);
    }
  }

  @Override
  public void addUserToOrg(String userId, String orgId) throws AuthorizableGatewayException {
    LOGGER.debug(String.format("Adding user %s to group %s", userId, orgId));
    try {
      if (!getUsersFromGroup(orgId).contains(userId)) {
        restTemplate.postForObject(createUrl(ApiEndpoints.USERS), new User(userId), String.class,
          orgId);
      } else {
        LOGGER.warn(String.format("Trying to add existing user %s in group %s", userId, orgId));
      }
    } catch (RestClientException e) {
      throw new AuthorizableGatewayException(String.format("Can't add user: %s", userId), e);
    }
  }

  @Override
  public void removeOrganization(String orgId) throws AuthorizableGatewayException {
    LOGGER.debug(String.format("Deleting group %s", orgId));
    try {
      if (getGroups().contains(orgId)) {
        restTemplate.delete(createUrl(ApiEndpoints.GROUP), orgId);
      }
    } catch (RestClientException e) {
      throw new AuthorizableGatewayException(String.format("Can't remove organization: %s", orgId),
        e);
    }
  }

  @Override
  public void removeUserFromOrg(String userId, String orgId) throws AuthorizableGatewayException {
    LOGGER.debug(String.format("Deleting user %s from group %s", userId, orgId));
    try {
      if (getUsersFromGroup(orgId).contains(userId)) {
        restTemplate.delete(createUrl(ApiEndpoints.USER),
            ImmutableMap.of("user", userId, "group", orgId));
      }
    } catch (RestClientException e) {
      throw new AuthorizableGatewayException(String.format("Can't remove user: %s from org: %s",
        userId, orgId), e);
    }
  }

  @Override
  public void addUser(String userId) throws AuthorizableGatewayException {
  }

  @Override
  public void removeUser(String userId) throws AuthorizableGatewayException {
  }

  @Override
  public String getName() {
    return NAME;
  }

  private String createUrl(String endpoint) {
    return groupMappingServiceUrl.concat(endpoint);
  }

  private List<String> getUsersFromGroup(String orgId) throws RestClientException {
    return Arrays.asList(restTemplate.getForObject(createUrl(ApiEndpoints.USERS), String[].class, orgId));
  }

  private List<String> getGroups() throws AuthorizableGatewayException {
    return Arrays.asList(restTemplate.getForObject(createUrl(ApiEndpoints.GROUPS), String[].class));
  }

  @VisibleForTesting void setGroupMappingServiceUrl(String url) {
    groupMappingServiceUrl = url;
  }

}
