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

package org.trustedanalytics.auth.gateway.hbase;

import java.io.IOException;

import org.apache.hadoop.hbase.NamespaceNotFoundException;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.security.access.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.auth.gateway.spi.Authorizable;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

import com.google.protobuf.ServiceException;

@Profile("hbase-auth-gateway")
@Configuration
public class HBaseGateway implements Authorizable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HBaseGateway.class);

    @Autowired
    private Connection connection;

    @Override
    public void addOrganization(String orgId) throws AuthorizableGatewayException {
        HBaseClient hBaseClient = HBaseClient.getNewInstance(connection);
        String namespaceName = orgId.replace("-", "");
        try {
            if(!hBaseClient.checkNamespaceExists(namespaceName))
                hBaseClient.createNamespace(namespaceName);

            String user = "@".concat(orgId);
            hBaseClient.grandPremisionOnNamespace(user, namespaceName, Permission.Action.CREATE);
        } catch (IOException | ServiceException e) {
            throw new AuthorizableGatewayException(e.getMessage(), e);
        }
    }

    @Override
    public void addUserToOrg(String userId, String orgId) throws AuthorizableGatewayException {
        // no-operation
    }

    @Override
    public void removeOrganization(String orgId) throws AuthorizableGatewayException {
        HBaseClient hBaseClient = HBaseClient.getNewInstance(connection);
        String namespaceName = orgId.replace("-", "");
        try {
            hBaseClient.removeNamespace(namespaceName);
        } catch (NamespaceNotFoundException e) {
            LOGGER.warn("Unable to delete namespace. Namespace named: " + namespaceName + " does not exist.");
        } catch (IOException e) {
            throw new AuthorizableGatewayException(e.getMessage(), e);
        }
    }

    @Override
    public void removeUserFromOrg(String userId, String orgId) throws AuthorizableGatewayException {
        // no-operation
    }

    @Override
    public void synchronize() throws AuthorizableGatewayException {
        // no-operation
    }

    @Override
    public String getName() {
        return "hbase";
    }

}
