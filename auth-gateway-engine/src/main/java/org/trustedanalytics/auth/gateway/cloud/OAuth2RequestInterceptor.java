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

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.trustedanalytics.auth.gateway.cloud.uaa.UaaApi;

import java.util.function.Supplier;

@Configuration
public class OAuth2RequestInterceptor implements RequestInterceptor {

    @Autowired
    private UaaApi uaaApi;

    public OAuth2RequestInterceptor() { }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("Authorization", "bearer " + uaaApi.authenticate().getAccessToken());
    }
}
