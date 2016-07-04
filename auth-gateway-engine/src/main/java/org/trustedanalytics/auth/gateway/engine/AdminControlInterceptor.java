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

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AdminControlInterceptor extends HandlerInterceptorAdapter {

  private static final String UBER_ADMIN_SCOPE = "cloud_controller.admin";

  private static final Logger LOGGER = LoggerFactory.getLogger(AdminControlInterceptor.class);

  private Supplier<String> tokenExtractor;

  public AdminControlInterceptor(Supplier<String> tokenExtractor) {
    super();
    this.tokenExtractor = tokenExtractor;
  }

  @Override
  public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) throws Exception {
    LOGGER.info("Check if cloudfoundry admin.");
    return checkIfAdmin();
  }

  private boolean checkIfAdmin() throws InvalidJwtException {
    JwtConsumer jwtConsumer = new JwtConsumerBuilder()
        .setSkipAllValidators()
        .setDisableRequireSignature()
        .setSkipSignatureVerification()
        .build();

    JwtContext context = jwtConsumer.process(tokenExtractor.get());

    return Optional.ofNullable(context.getJwtClaims())
            .map(claim -> claim.getClaimsMap())
            .map(claimsMap -> claimsMap.get("scope"))
            .filter(scopes -> scopes.toString().contains(UBER_ADMIN_SCOPE))
            .isPresent();
  }
}
