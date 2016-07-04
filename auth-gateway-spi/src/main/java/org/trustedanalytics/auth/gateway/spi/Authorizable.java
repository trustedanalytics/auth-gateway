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
package org.trustedanalytics.auth.gateway.spi;

import com.google.common.annotations.Beta;

/**
 * Authorizable interface stands for single gateway to be called by auth-gateway-engine.
 *
 * Engine will call all found Authorizables in parallel and it is possible that Engine will
 * sometimes retry its calls. That's why all operations from this interface should be idempotent.
 */
@Beta
public interface Authorizable {

    void addOrganization(String orgId) throws AuthorizableGatewayException;

    void addUserToOrg(String userId, String orgId) throws AuthorizableGatewayException;

    void removeOrganization(String orgId) throws AuthorizableGatewayException;

    void removeUserFromOrg(String userId, String orgId) throws AuthorizableGatewayException;

    void synchronize() throws AuthorizableGatewayException;

    String getName();
}
