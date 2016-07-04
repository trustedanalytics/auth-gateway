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

import org.junit.Before;
import org.junit.Test;
import org.trustedanalytics.auth.gateway.state.State;
import org.trustedanalytics.auth.gateway.zookeeper.ZookeeperClient;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertEquals;

public class StateTest {
    public static final String VERSION = "None";
    public static final String ORG = "RANDOM_ORG_ID";
    public static final String USER = "RANDOM_USER";

    private ZookeeperClient client;

    private State state;

    @Before
    public void setUp() {
        client = mock(ZookeeperClient.class);
        state = new State(client, VERSION);
    }

    @Test
    public void state_init_validateInitCreation() throws Exception {
        doReturn(false).when(client).checkExists(State.BASE_NODE);

        state.init();

        verify(client).checkExists(State.BASE_NODE);
        verify(client).createNode(State.BASE_NODE, VERSION.getBytes());
        verify(client, times(0)).deleteNode(State.BASE_NODE);
        verify(client, times(0)).getNodeData(State.BASE_NODE);
    }

    @Test
    public void state_init_doNothingWhenVersionMatch() throws Exception {
        doReturn(true).when(client).checkExists(State.BASE_NODE);
        doReturn(VERSION.getBytes()).when(client).getNodeData(State.BASE_NODE);

        state.init();

        verify(client).checkExists(State.BASE_NODE);
        verify(client).getNodeData(State.BASE_NODE);
        verify(client, times(0)).deleteNode(State.BASE_NODE);
        verify(client).createNode(State.BASE_NODE, VERSION.getBytes());
    }

    @Test
    public void state_init_recreateWhenVersionDoesntMatch() throws Exception {
        doReturn(true).when(client).checkExists(State.BASE_NODE);
        doReturn("".getBytes()).when(client).getNodeData(State.BASE_NODE);

        state.init();

        verify(client).checkExists(State.BASE_NODE);
        verify(client).getNodeData(State.BASE_NODE);
        verify(client).deleteNode(State.BASE_NODE);
        verify(client).createNode(State.BASE_NODE, VERSION.getBytes());
    }

    @Test
    public void state_setOrgState_setStateToTrue() throws Exception {
        state.setValidState(ORG);

        verify(client).createNode(getPath(ORG), new byte[] { });
    }

    @Test
    public void state_setOrgState_setStateToFalse() throws Exception {
        state.unsetValidState(ORG);

        verify(client).deleteNode(getPath(ORG));
    }

    @Test
    public void state_setUserState_setStateToTrue() throws Exception {
        state.unsetValidState(ORG, USER);

        verify(client).deleteNode(getPath(ORG, USER));
    }

    @Test
    public void state_getUserState_() throws Exception {
        doReturn(true).when(client).checkExists(getPath(ORG, USER));

        assertEquals(state.getValidState(ORG, USER), true);
        verify(client).checkExists(getPath(ORG, USER));
    }

    @Test
    public void state_getOrgState_() throws Exception {
        doReturn(true).when(client).checkExists(getPath(ORG));

        assertEquals(state.getValidState(ORG), true);
        verify(client).checkExists(getPath(ORG));
    }

    private String getPath(String... args)
    {
        return String.join("/", State.BASE_NODE, String.join("/", args));
    }
}
