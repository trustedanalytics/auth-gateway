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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.auth.gateway.hgm.entity.User;
import org.trustedanalytics.auth.gateway.hgm.utils.ApiEndpoints;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class HgmGatewayTest {

  @Mock
  private RestTemplate restTemplate;

  @InjectMocks
  private HgmGateway hgmGateway;

  private static final String ORG_ID = "test_org_id";

  private static final String USER_ID = "test_user_id";

  private static final String ORG_ADMIN = "test_org_id_admin";

  private static final String HGM_TEST_URL = "http://test_url.domain";

  @Before
  public void initialize() {
    hgmGateway.setGroupMappingServiceUrl(HGM_TEST_URL);
  }

  @Test
  public void addOrganization_hadoopGroupMappingServiceCalled_creationSuccess()
      throws AuthorizableGatewayException {
    when(restTemplate.getForObject(
        eq(HGM_TEST_URL.concat(ApiEndpoints.GROUPS)), eq(String[].class)))
        .thenReturn(new String []{});
    hgmGateway.addOrganization(ORG_ID);
    verify(restTemplate).postForObject(eq(HGM_TEST_URL.concat(ApiEndpoints.USERS)),
        eq(new User(ORG_ADMIN)), eq(String.class), eq(ORG_ID));
  }

  @Test
  public void addOrganization_hadoopGroupMappingServiceCalled_alreadyExsits()
      throws AuthorizableGatewayException {
    when(restTemplate.getForObject(
      eq(HGM_TEST_URL.concat(ApiEndpoints.GROUPS)), eq(String[].class)))
      .thenReturn(new String []{ORG_ID});
    hgmGateway.addOrganization(ORG_ID);
  }

  @Test
  public void addUserToOrg_hadoopGroupMappingServiceCalled_creationSuccess()
      throws AuthorizableGatewayException {
    when(restTemplate.getForObject(
      eq(HGM_TEST_URL.concat(ApiEndpoints.USERS)), eq(String[].class),  eq(ORG_ID)))
      .thenReturn(new String []{});
    hgmGateway.addUserToOrg(USER_ID, ORG_ID);
    verify(restTemplate).postForObject(eq(HGM_TEST_URL.concat(ApiEndpoints.USERS)),
        eq(new User(USER_ID)), eq(String.class), eq(ORG_ID));
  }

  @Test
  public void addUserToOrg_hadoopGroupMappingServiceCalled_alreadyExsits()
      throws AuthorizableGatewayException {
    when(restTemplate.getForObject(
      eq(HGM_TEST_URL.concat(ApiEndpoints.USERS)), eq(String[].class),  eq(ORG_ID)))
      .thenReturn(new String []{USER_ID});
    hgmGateway.addUserToOrg(USER_ID, ORG_ID);
    verify(restTemplate, never()).postForObject(any(), any(), any());
  }

  @Test
  public void removeUserFromOrg_hadoopGroupMappingServiceCalled_deletionSuccess()
      throws AuthorizableGatewayException {
    when(restTemplate.getForObject(
        eq(HGM_TEST_URL.concat(ApiEndpoints.USERS)), eq(String[].class),  eq(ORG_ID)))
        .thenReturn(new String []{USER_ID});
    hgmGateway.removeUserFromOrg(USER_ID, ORG_ID);
    verify(restTemplate).delete(eq(HGM_TEST_URL.concat(ApiEndpoints.USER)),
        eq(ImmutableMap.of("user", USER_ID, "group", ORG_ID)));
  }

  @Test
  public void removeUserFromOrg_hadoopGroupMappingServiceCalled_userNotExsits()
      throws AuthorizableGatewayException {
    when(restTemplate.getForObject(
        eq(HGM_TEST_URL.concat(ApiEndpoints.USERS)), eq(String[].class),  eq(ORG_ID)))
        .thenReturn(new String []{});
    hgmGateway.removeUserFromOrg(USER_ID, ORG_ID);
  }

  @Test
  public void removeOrganization_hadoopGroupMappingServiceCalled_deletionSuccess()
      throws AuthorizableGatewayException {
    when(restTemplate.getForObject(
        eq(HGM_TEST_URL.concat(ApiEndpoints.GROUPS)), eq(String[].class)))
        .thenReturn(new String []{ORG_ID});
    hgmGateway.removeOrganization(ORG_ID);
    verify(restTemplate).delete(eq(HGM_TEST_URL.concat(ApiEndpoints.GROUP)), eq(ORG_ID));
  }

  @Test
  public void removeOrganization_hadoopGroupMappingServiceCalled_organizationNotExists()
      throws AuthorizableGatewayException {
    when(restTemplate.getForObject(
        eq(HGM_TEST_URL.concat(ApiEndpoints.GROUPS)), eq(String[].class)))
        .thenReturn(new String []{ORG_ID});
    hgmGateway.removeOrganization(ORG_ID);
  }

}
