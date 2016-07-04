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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Component
public class OAuth2Helper {

  @Autowired
  ClientDetailsService clientDetailsService;

  @Autowired
  @Qualifier("defaultAuthorizationServerTokenServices")
  AuthorizationServerTokenServices tokenService;

  public RequestPostProcessor bearerToken(final String clientId) {
    return request -> {
      OAuth2AccessToken token = createAccessToken(clientId);
      request.addHeader("Authorization", "Bearer " + token.getValue());
      return request;
    };
  }

  OAuth2AccessToken createAccessToken(final String clientId) {
    // Look up authorities, resourceIds and scopes based on clientId
    ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
    Collection<GrantedAuthority> authorities = client.getAuthorities();
    Set<String> resourceIds = client.getResourceIds();
    Set<String> scopes = client.getScope();

    // Default values for other parameters
    Map<String, String> requestParameters = Collections.emptyMap();
    boolean approved = true;
    String redirectUrl = null;
    Set<String> responseTypes = Collections.emptySet();
    Map<String, Serializable> extensionProperties = Collections.emptyMap();

    // Create request
    OAuth2Request oAuth2Request =
        new OAuth2Request(requestParameters, clientId, authorities, approved, scopes,
                          resourceIds, redirectUrl, responseTypes, extensionProperties);
    // Create OAuth2AccessToken
    User userPrincipal = new User("user", "", true, true, true, true, authorities);
    UsernamePasswordAuthenticationToken
        authenticationToken = new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
    OAuth2Authentication auth = new OAuth2Authentication(oAuth2Request, authenticationToken);
    return tokenService.createAccessToken(auth);
  }
}
