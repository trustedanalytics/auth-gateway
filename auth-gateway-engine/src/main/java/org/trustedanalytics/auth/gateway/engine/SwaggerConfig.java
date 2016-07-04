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

import com.google.common.base.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    Docket orgsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .ignoredParameterTypes(Authentication.class)
                .apiInfo(apiInfo())
                .select()
                .paths(orgsPaths())
                .build()
                .useDefaultResponseMessages(false);
    }

    private Predicate<String> orgsPaths() {
        return or(regex("/organizations/.*"), regex("/users/.*"), regex("/synchronize.*"), regex("/state.*"),
                regex("/jobs.*"));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Auth-gateway API")
                .description("Api endpoints for adding and removing authorization data for hadoop components in TAP")
                .license("Apache License Version 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0")
                .build();
    }
}
