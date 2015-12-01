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

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.trustedanalytics.auth.gateway.spi.Authorizable;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

public class EngineTest {

    private static final String USER_ID = "666";
    private static final String USER_NAME = "laksj";
    private static final String ORG_ID = "897351";
    private static final String ORG_NAME = "alkheaefg";
    private static final String EXCEPTION_MESSAGE = "Something went wrong";
    private static final String AUTHORIZABLE1_NAME = "AUTH_1";
    private static final String AUTHORIZABLE2_NAME = "AUTH_2";
    private static final long ENGINE_TIMEOUT_IN_SECONDS = 1;
    private static final long LONG_CALL_DURATION_IN_SECONDS = 2;

    private Authorizable authorizableMock1;
    private Authorizable authorizableMock2;
    List<Authorizable> listOfAuthorizables;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        authorizableMock1 = mock(Authorizable.class);
        when(authorizableMock1.getName()).thenReturn(AUTHORIZABLE1_NAME);
        authorizableMock2 = mock(Authorizable.class);
        when(authorizableMock2.getName()).thenReturn(AUTHORIZABLE2_NAME);

        listOfAuthorizables = prepareAuthorizablesList(authorizableMock1, authorizableMock2);
    }

    @Test
    public void addUser_allAuthorizablesOk_authorizablesMethodsInvoked() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // when
        engine.addUser(USER_ID, USER_NAME);

        // then
        verify(authorizableMock1).addUser(USER_ID, USER_NAME);
        verify(authorizableMock2).addUser(USER_ID, USER_NAME);
    }

    @Test
    public void addUser_tooLongAuthorizableCall_exceptionThrown()
            throws AuthorizableGatewayException {
        // given
        doAnswer(longAnswer()).when(authorizableMock1).addUser(USER_ID, USER_NAME);

        // when
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error adding user");
        thrown.expectMessage("TimeoutException");
        engine.addUser(USER_ID, USER_NAME);
    }
    
    @Test
    public void addUser_exceptionFromAuthorizable_exceptionThrown()
            throws AuthorizableGatewayException {
        // given
        doThrow(new AuthorizableGatewayException(EXCEPTION_MESSAGE)).when(authorizableMock1)
                .addUser(USER_ID, USER_NAME);

        // when
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error adding user");
        thrown.expectMessage(AUTHORIZABLE1_NAME + " failed: " + EXCEPTION_MESSAGE);
        engine.addUser(USER_ID, USER_NAME);

    }
   
    @Test
    public void addOrganization_allAuthorizablesOk_authorizablesMethodsInvoked() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // when
        engine.addOrganization(ORG_ID, ORG_NAME);

        // then
        verify(authorizableMock1).addOrganization(ORG_ID, ORG_NAME);
        verify(authorizableMock2).addOrganization(ORG_ID, ORG_NAME);
    }

    @Test
    public void addOrganization_tooLongAuthorizableCall_exceptionThrown()
            throws AuthorizableGatewayException {
        // given
        doAnswer(longAnswer()).when(authorizableMock1).addOrganization(ORG_ID, ORG_NAME);

        // when
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error adding organization");
        thrown.expectMessage("TimeoutException");
        engine.addOrganization(ORG_ID, ORG_NAME);
    }

    @Test
    public void addOrganization_exceptionFromAuthorizable_exceptionThrown()
            throws AuthorizableGatewayException {
        // given
        doThrow(new AuthorizableGatewayException(EXCEPTION_MESSAGE)).when(authorizableMock1)
                .addOrganization(ORG_ID, ORG_NAME);

        // when
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error adding organization");
        thrown.expectMessage(AUTHORIZABLE1_NAME + " failed: " + EXCEPTION_MESSAGE);
        engine.addOrganization(ORG_ID, ORG_NAME);

    }
    
    @Test
    public void addUserToOrg_allAuthorizablesOk_authorizablesMethodsInvoked() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // when
        engine.addUserToOrg(USER_ID, ORG_ID);

        // then
        verify(authorizableMock1).addUserToOrg(USER_ID, ORG_ID);
        verify(authorizableMock2).addUserToOrg(USER_ID, ORG_ID);
    }

    @Test
    public void addUserToOrg_tooLongAuthorizableCall_exceptionThrown()
            throws AuthorizableGatewayException {
        // given
        doAnswer(longAnswer()).when(authorizableMock1).addUserToOrg(USER_ID, ORG_ID);

        // when
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error adding user to organization");
        thrown.expectMessage("TimeoutException");
        engine.addUserToOrg(USER_ID, ORG_ID);
    }

    @Test
    public void addUserToOrg_exceptionFromAuthorizable_exceptionThrown()
            throws AuthorizableGatewayException {
        // given
        doThrow(new AuthorizableGatewayException(EXCEPTION_MESSAGE)).when(authorizableMock1)
                .addUserToOrg(USER_ID, ORG_ID);

        // when
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error adding user to organization");
        thrown.expectMessage(AUTHORIZABLE1_NAME + " failed: " + EXCEPTION_MESSAGE);
        engine.addUserToOrg(USER_ID, ORG_ID);

    }
    
    @Test
    public void removeUser_allAuthorizablesOk_authorizablesMethodsInvoked() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // when
        engine.removeUser(USER_ID, USER_NAME);

        // then
        verify(authorizableMock1).removeUser(USER_ID, USER_NAME);
        verify(authorizableMock2).removeUser(USER_ID, USER_NAME);
    }

    @Test
    public void removeUser_tooLongAuthorizableCall_exceptionThrown()
            throws AuthorizableGatewayException {
        // given
        doAnswer(longAnswer()).when(authorizableMock1).removeUser(USER_ID, USER_NAME);

        // when
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error removing user");
        thrown.expectMessage("TimeoutException");
        engine.removeUser(USER_ID, USER_NAME);
    }

    @Test
    public void removeUser_exceptionFromAuthorizable_exceptionThrown()
            throws AuthorizableGatewayException {
        // given
        doThrow(new AuthorizableGatewayException(EXCEPTION_MESSAGE)).when(authorizableMock1)
                .removeUser(USER_ID, USER_NAME);

        // when
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error removing user");
        thrown.expectMessage(AUTHORIZABLE1_NAME + " failed: " + EXCEPTION_MESSAGE);
        engine.removeUser(USER_ID, USER_NAME);

    }
    
    @Test
    public void removeOrganization_allAuthorizablesOk_authorizablesMethodsInvoked() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // when
        engine.removeOrganization(ORG_ID, ORG_NAME);

        // then
        verify(authorizableMock1).removeOrganization(ORG_ID, ORG_NAME);
        verify(authorizableMock2).removeOrganization(ORG_ID, ORG_NAME);
    }

    @Test
    public void removeOrganization_tooLongAuthorizableCall_exceptionThrown()
            throws AuthorizableGatewayException {
        // given
        doAnswer(longAnswer()).when(authorizableMock1).removeOrganization(ORG_ID, ORG_NAME);

        // when
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error removing organization");
        thrown.expectMessage("TimeoutException");
        engine.removeOrganization(ORG_ID, ORG_NAME);
    }

    @Test
    public void removeOrganization_exceptionFromAuthorizable_exceptionThrown()
            throws AuthorizableGatewayException {
        // given
        doThrow(new AuthorizableGatewayException(EXCEPTION_MESSAGE)).when(authorizableMock1)
                .removeOrganization(ORG_ID, ORG_NAME);

        // when
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error removing organization");
        thrown.expectMessage(AUTHORIZABLE1_NAME + " failed: " + EXCEPTION_MESSAGE);
        engine.removeOrganization(ORG_ID, ORG_NAME);

    }
    
    @Test
    public void removeUserFromOrg_allAuthorizablesOk_authorizablesMethodsInvoked() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // when
        engine.removeUserFromOrg(USER_ID, ORG_ID);

        // then
        verify(authorizableMock1).removeUserFromOrg(USER_ID, ORG_ID);
        verify(authorizableMock2).removeUserFromOrg(USER_ID, ORG_ID);
    }

    @Test
    public void removeUserFromOrg_tooLongAuthorizableCall_exceptionThrown()
            throws AuthorizableGatewayException {
        // given
        doAnswer(longAnswer()).when(authorizableMock1).removeUserFromOrg(USER_ID, ORG_ID);

        // when
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error removing user from organization");
        thrown.expectMessage("TimeoutException");
        engine.removeUserFromOrg(USER_ID, ORG_ID);
    }

    @Test
    public void removeUserFromOrg_exceptionFromAuthorizable_exceptionThrown()
            throws AuthorizableGatewayException {
        // given
        doThrow(new AuthorizableGatewayException(EXCEPTION_MESSAGE)).when(authorizableMock1)
                .removeUserFromOrg(USER_ID, ORG_ID);

        // when
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS);

        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error removing user from organization");
        thrown.expectMessage(AUTHORIZABLE1_NAME + " failed: " + EXCEPTION_MESSAGE);
        engine.removeUserFromOrg(USER_ID, ORG_ID);

    }

    private List<Authorizable> prepareAuthorizablesList(Authorizable... authorizables) {
        List<Authorizable> list = new LinkedList<>();

        for (Authorizable authorizable : authorizables) {
            list.add(authorizable);
        }

        return list;
    }

    private Answer<Void> longAnswer() {
        return new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(LONG_CALL_DURATION_IN_SECONDS * 1000);
                return null;
            }

        };
    }

}

