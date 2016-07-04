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
package org.trustedanalytics.auth.gateway.engine.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.trustedanalytics.auth.gateway.engine.AuthgatewayResponse;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrganizationState implements AuthgatewayResponse {

    private String name;

    private String guid;

    private List<UserState> users;

    @JsonProperty("synchronized")
    private boolean synchronizedState;

    public OrganizationState(String name, String guid) {
        this.name = name;
        this.guid = guid;
        this.users = new ArrayList<>();
        this.synchronizedState = false;
    }
}
