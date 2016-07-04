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
package org.trustedanalytics.auth.gateway.state;

import com.google.common.annotations.VisibleForTesting;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;
import org.trustedanalytics.auth.gateway.zookeeper.ZookeeperClient;

import java.util.Objects;

public class State {

    public static final String BASE_NODE = "/state";

    private ZookeeperClient client;
    private String version;

    public State(ZookeeperClient client, String version)
    {
        this.client = client;
        this.version = version;
    }

    public void init() throws Exception
    {
        if(client.checkExists(BASE_NODE))
        {
            String newVersion = new String(client.getNodeData(BASE_NODE));

            if(!Objects.equals(newVersion, this.version))
                client.deleteNode(BASE_NODE);
        }

        client.createNode(BASE_NODE, this.version.getBytes());
    }

    private String getPath(String... args)
    {
        return String.join("/", BASE_NODE, String.join("/", args));
    }

    public void setValidState(String orgId) throws AuthorizableGatewayException
    {
        try {
            client.createNode(getPath(orgId), new byte[]{});
        }
        catch (Exception e)
        {
            throw new AuthorizableGatewayException("Setting valid state failed", e);
        }
    }

    public void setValidState(String orgId, String userId) throws AuthorizableGatewayException
    {
        try {
            client.createNode(getPath(orgId, userId), new byte[]{});
        }
        catch (Exception e)
        {
            throw new AuthorizableGatewayException("Setting valid state failed", e);
        }
    }

    public void unsetValidState(String orgId) throws AuthorizableGatewayException
    {
        try {
            client.deleteNode(getPath(orgId));
        }
        catch (Exception e)
        {
            throw new AuthorizableGatewayException("Setting valid state failed", e);
        }
    }

    public void unsetValidState(String orgId, String userId) throws AuthorizableGatewayException
    {
        try {
            client.deleteNode(getPath(orgId, userId));
        }
        catch (Exception e)
        {
            throw new AuthorizableGatewayException("Setting valid state failed", e);
        }
    }

    public boolean getValidState(String orgId) throws AuthorizableGatewayException
    {
        try {
            return client.checkExists(getPath(orgId));
        }
        catch (Exception e)
        {
            throw new AuthorizableGatewayException("Setting valid state failed", e);
        }
    }

    public boolean getValidState(String orgId, String userId) throws AuthorizableGatewayException
    {
        try {
            return client.checkExists(getPath(orgId, userId));
        }
        catch (Exception e)
        {
            throw new AuthorizableGatewayException("Setting valid state failed", e);
        }
    }
}
