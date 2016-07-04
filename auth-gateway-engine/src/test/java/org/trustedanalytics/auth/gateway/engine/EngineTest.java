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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.trustedanalytics.auth.gateway.cloud.Cloud;
import org.trustedanalytics.auth.gateway.engine.response.OrganizationState;
import org.trustedanalytics.auth.gateway.engine.response.PlatformState;
import org.trustedanalytics.auth.gateway.engine.response.UserState;
import org.trustedanalytics.auth.gateway.spi.Authorizable;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;
import org.trustedanalytics.auth.gateway.state.State;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EngineTest {

    private static final String USER_ID = "666";
    private static final String ORG_ID = "897351";
    private static final String EXCEPTION_MESSAGE = "Something went wrong";
    private static final String AUTHORIZABLE1_NAME = "AUTH_1";
    private static final String AUTHORIZABLE2_NAME = "AUTH_2";
    private static final long ENGINE_TIMEOUT_IN_SECONDS = 1;
    private static final long LONG_CALL_DURATION_IN_SECONDS = 2;

    private Authorizable authorizableMock1;
    private Authorizable authorizableMock2;
    List<Authorizable> listOfAuthorizables;

    private Cloud cloud;

    private State state;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        authorizableMock1 = mock(Authorizable.class);
        when(authorizableMock1.getName()).thenReturn(AUTHORIZABLE1_NAME);
        authorizableMock2 = mock(Authorizable.class);
        when(authorizableMock2.getName()).thenReturn(AUTHORIZABLE2_NAME);
        cloud = mock(Cloud.class);
        state = mock(State.class);

        listOfAuthorizables = prepareAuthorizablesList(authorizableMock1, authorizableMock2);
    }

    @Test
    public void addOrganization_allAuthorizablesOk_authorizablesMethodsInvoked() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);

        List<OrganizationState> orgs = Arrays.asList(new OrganizationState(ORG_ID, ORG_ID));
        orgs.get(0).setUsers(Arrays.asList(new UserState(USER_ID, USER_ID)));

        doReturn(orgs).when(cloud).getOrganizations();
        doReturn(true).when(state).getValidState(ORG_ID);
        doReturn(true).when(state).getValidState(ORG_ID, USER_ID);

        // when
        engine.addOrganization(ORG_ID);

        // then
        verify(authorizableMock1).addOrganization(ORG_ID);
        verify(authorizableMock2).addOrganization(ORG_ID);
    }

    @Test
    public void addOrganization_tooLongAuthorizableCall_exceptionThrown()
            throws AuthorizableGatewayException {
        // given
        doAnswer(longAnswer()).when(authorizableMock1).addOrganization(ORG_ID);

        // when
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);

        List<OrganizationState> orgs = Arrays.asList(new OrganizationState(ORG_ID, ORG_ID));
        orgs.get(0).setUsers(Arrays.asList(new UserState(USER_ID, USER_ID)));
        doReturn(orgs).when(cloud).getOrganizations();
        doReturn(true).when(state).getValidState(ORG_ID);
        doReturn(true).when(state).getValidState(ORG_ID, USER_ID);

        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error adding organization");
        thrown.expectMessage("TimeoutException");
        engine.addOrganization(ORG_ID);
    }

    @Test
    public void addOrganization_exceptionFromAuthorizable_exceptionThrown()
            throws AuthorizableGatewayException {
        // given
        doThrow(new AuthorizableGatewayException(EXCEPTION_MESSAGE)).when(authorizableMock1)
                .addOrganization(ORG_ID);

        // when
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);

        List<OrganizationState> orgs = Arrays.asList(new OrganizationState(ORG_ID, ORG_ID));
        orgs.get(0).setUsers(Arrays.asList(new UserState(USER_ID, USER_ID)));
        doReturn(orgs).when(cloud).getOrganizations();
        doReturn(true).when(state).getValidState(ORG_ID);
        doReturn(true).when(state).getValidState(ORG_ID, USER_ID);


        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error adding organization");
        thrown.expectMessage(AUTHORIZABLE1_NAME + " failed: " + EXCEPTION_MESSAGE);
        engine.addOrganization(ORG_ID);

    }

    @Test
    public void addUserToOrg_allAuthorizablesOk_authorizablesMethodsInvoked() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);

        List<OrganizationState> orgs = Arrays.asList(new OrganizationState(ORG_ID, ORG_ID));
        orgs.get(0).setUsers(Arrays.asList(new UserState(USER_ID, USER_ID)));

        doReturn(orgs).when(cloud).getOrganizations();
        doReturn(true).when(state).getValidState(ORG_ID);
        doReturn(true).when(state).getValidState(ORG_ID, USER_ID);

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
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);

        List<OrganizationState> orgs = Arrays.asList(new OrganizationState(ORG_ID, ORG_ID));
        orgs.get(0).setUsers(Arrays.asList(new UserState(USER_ID, USER_ID)));
        doReturn(orgs).when(cloud).getOrganizations();
        doReturn(true).when(state).getValidState(ORG_ID);
        doReturn(true).when(state).getValidState(ORG_ID, USER_ID);

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
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);

        List<OrganizationState> orgs = Arrays.asList(new OrganizationState(ORG_ID, ORG_ID));
        orgs.get(0).setUsers(Arrays.asList(new UserState(USER_ID, USER_ID)));
        doReturn(orgs).when(cloud).getOrganizations();
        doReturn(true).when(state).getValidState(ORG_ID);
        doReturn(true).when(state).getValidState(ORG_ID, USER_ID);

        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error adding user to organization");
        thrown.expectMessage(AUTHORIZABLE1_NAME + " failed: " + EXCEPTION_MESSAGE);
        engine.addUserToOrg(USER_ID, ORG_ID);

    }

    @Test
    public void removeOrganization_allAuthorizablesOk_authorizablesMethodsInvoked() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);

        // when
        engine.removeOrganization(ORG_ID);

        // then
        verify(authorizableMock1).removeOrganization(ORG_ID);
        verify(authorizableMock2).removeOrganization(ORG_ID);
    }

    @Test
    public void removeOrganization_tooLongAuthorizableCall_exceptionThrown()
            throws AuthorizableGatewayException {
        // given
        doAnswer(longAnswer()).when(authorizableMock1).removeOrganization(ORG_ID);

        // when
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);


        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error removing organization");
        thrown.expectMessage("TimeoutException");
        engine.removeOrganization(ORG_ID);
    }

    @Test
    public void removeOrganization_exceptionFromAuthorizable_exceptionThrown()
            throws AuthorizableGatewayException {
        // given
        doThrow(new AuthorizableGatewayException(EXCEPTION_MESSAGE)).when(authorizableMock1)
                .removeOrganization(ORG_ID);

        // when
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);

        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error removing organization");
        thrown.expectMessage(AUTHORIZABLE1_NAME + " failed: " + EXCEPTION_MESSAGE);
        engine.removeOrganization(ORG_ID);

    }

    @Test
    public void removeUserFromOrg_allAuthorizablesOk_authorizablesMethodsInvoked() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);

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
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);

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
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);

        // then
        thrown.expect(AuthorizableGatewayException.class);
        thrown.expectMessage("Error removing user from organization");
        thrown.expectMessage(AUTHORIZABLE1_NAME + " failed: " + EXCEPTION_MESSAGE);
        engine.removeUserFromOrg(USER_ID, ORG_ID);

    }

    @Test
    public void synchronize_createOrganizations_authorizablesMethodsInvoked() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);
        List<OrganizationState> organizations = Arrays.asList(new OrganizationState("organization1", "organization1"),
                new OrganizationState("organization2", "organization2"));

        List<UserState> users1 = Arrays.asList(new UserState("user1", "user1"), new UserState("user2", "user2"));
        List<UserState> users2 = Arrays.asList(new UserState("user3", "user3"), new UserState("user4", "user4"));

        organizations.get(0).setUsers(users1);
        organizations.get(1).setUsers(users2);

        // when
        doReturn(organizations).when(cloud).getOrganizations();

        // then
        engine.synchronize();

        verify(authorizableMock1).addOrganization(organizations.get(0).getGuid());
        verify(authorizableMock1).addOrganization(organizations.get(1).getGuid());
        verify(authorizableMock1).addUserToOrg(users1.get(0).getGuid(), organizations.get(0).getGuid());
        verify(authorizableMock1).addUserToOrg(users1.get(1).getGuid(), organizations.get(0).getGuid());
        verify(authorizableMock1).addUserToOrg(users2.get(0).getGuid(), organizations.get(1).getGuid());
        verify(authorizableMock1).addUserToOrg(users2.get(1).getGuid(), organizations.get(1).getGuid());

        verify(authorizableMock2).addOrganization(organizations.get(0).getGuid());
        verify(authorizableMock2).addOrganization(organizations.get(1).getGuid());
        verify(authorizableMock2).addUserToOrg(users1.get(0).getGuid(), organizations.get(0).getGuid());
        verify(authorizableMock2).addUserToOrg(users1.get(1).getGuid(), organizations.get(0).getGuid());
        verify(authorizableMock2).addUserToOrg(users2.get(0).getGuid(), organizations.get(1).getGuid());
        verify(authorizableMock2).addUserToOrg(users2.get(1).getGuid(), organizations.get(1).getGuid());
    }

    @Test
    public void synchronize_createOrganization_authorizablesMethodsInvoked() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);
        List<OrganizationState> organizations = Arrays.asList(new OrganizationState("organization1", "organization1"),
                new OrganizationState("organization2", "organization2"));

        List<UserState> users1 = Arrays.asList(new UserState("user1", "user1"), new UserState("user2", "user2"));
        List<UserState> users2 = Arrays.asList(new UserState("user3", "user3"), new UserState("user4", "user4"));

        organizations.get(0).setUsers(users1);
        organizations.get(1).setUsers(users2);

        // when
        doReturn(organizations).when(cloud).getOrganizations();

        // then
        engine.synchronizeOrg(organizations.get(0).getGuid());

        verify(authorizableMock1).addOrganization(organizations.get(0).getGuid());
        verify(authorizableMock1, times(0)).addOrganization(organizations.get(1).getGuid());
        verify(authorizableMock1).addUserToOrg(users1.get(0).getGuid(), organizations.get(0).getGuid());
        verify(authorizableMock1).addUserToOrg(users1.get(1).getGuid(), organizations.get(0).getGuid());
        verify(authorizableMock1, times(0)).addUserToOrg(users2.get(0).getGuid(), organizations.get(1).getGuid());
        verify(authorizableMock1, times(0)).addUserToOrg(users2.get(1).getGuid(), organizations.get(1).getGuid());

        verify(authorizableMock2).addOrganization(organizations.get(0).getGuid());
        verify(authorizableMock2, times(0)).addOrganization(organizations.get(1).getGuid());
        verify(authorizableMock2).addUserToOrg(users1.get(0).getGuid(), organizations.get(0).getGuid());
        verify(authorizableMock2).addUserToOrg(users1.get(1).getGuid(), organizations.get(0).getGuid());
        verify(authorizableMock2, times(0)).addUserToOrg(users2.get(0).getGuid(), organizations.get(1).getGuid());
        verify(authorizableMock2, times(0)).addUserToOrg(users2.get(1).getGuid(), organizations.get(1).getGuid());
    }

    @Test(expected = AuthorizableGatewayException.class)
    public void synchronize_createOrganization_notFoundOrg() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);
        List<OrganizationState> organizations = Arrays.asList(new OrganizationState("organization1", "organization1"),
                new OrganizationState("organization2", "organization2"));

        List<UserState> users1 = Arrays.asList(new UserState("user1", "user1"), new UserState("user2", "user2"));
        List<UserState> users2 = Arrays.asList(new UserState("user3", "user3"), new UserState("user4", "user4"));

        organizations.get(0).setUsers(users1);
        organizations.get(1).setUsers(users2);

        // when
        doReturn(organizations).when(cloud).getOrganizations();

        // then
        engine.synchronizeOrg("random");
    }

    @Test
    public void synchronize_createUserInOrganization_addUserToOrg() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);
        List<OrganizationState> organizations = Arrays.asList(new OrganizationState("organization1", "organization1"),
                new OrganizationState("organization2", "organization2"));

        List<UserState> users1 = Arrays.asList(new UserState("user1", "user1"), new UserState("user2", "user2"));
        List<UserState> users2 = Arrays.asList(new UserState("user3", "user3"), new UserState("user4", "user4"));

        organizations.get(0).setUsers(users1);
        organizations.get(1).setUsers(users2);

        // when
        doReturn(organizations).when(cloud).getOrganizations();
        doReturn(true).when(state).getValidState(organizations.get(0).getGuid());

        // then
        engine.synchronizeUser(organizations.get(0).getGuid(), users1.get(0).getGuid());

        verify(authorizableMock1, times(0)).addOrganization(organizations.get(0).getGuid());
        verify(authorizableMock1, times(0)).addOrganization(organizations.get(1).getGuid());
        verify(authorizableMock1).addUserToOrg(users1.get(0).getGuid(), organizations.get(0).getGuid());
        verify(authorizableMock1, times(0)).addUserToOrg(users1.get(1).getGuid(), organizations.get(0).getGuid());
        verify(authorizableMock1, times(0)).addUserToOrg(users2.get(0).getGuid(), organizations.get(1).getGuid());
        verify(authorizableMock1, times(0)).addUserToOrg(users2.get(1).getGuid(), organizations.get(1).getGuid());

        verify(authorizableMock2, times(0)).addOrganization(organizations.get(0).getGuid());
        verify(authorizableMock2, times(0)).addOrganization(organizations.get(1).getGuid());
        verify(authorizableMock2).addUserToOrg(users1.get(0).getGuid(), organizations.get(0).getGuid());
        verify(authorizableMock2, times(0)).addUserToOrg(users1.get(1).getGuid(), organizations.get(0).getGuid());
        verify(authorizableMock2, times(0)).addUserToOrg(users2.get(0).getGuid(), organizations.get(1).getGuid());
        verify(authorizableMock2, times(0)).addUserToOrg(users2.get(1).getGuid(), organizations.get(1).getGuid());
    }

    @Test(expected = AuthorizableGatewayException.class)
    public void synchronize_createUserInOrganization_notSynchronizedOrg() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);
        List<OrganizationState> organizations = Arrays.asList(new OrganizationState("organization1", "organization1"),
                new OrganizationState("organization2", "organization2"));

        List<UserState> users1 = Arrays.asList(new UserState("user1", "user1"), new UserState("user2", "user2"));
        List<UserState> users2 = Arrays.asList(new UserState("user3", "user3"), new UserState("user4", "user4"));

        organizations.get(0).setUsers(users1);
        organizations.get(1).setUsers(users2);

        // when
        doReturn(organizations).when(cloud).getOrganizations();
        doReturn(false).when(state).getValidState(organizations.get(0).getGuid());

        // then
        engine.synchronizeUser(organizations.get(0).getGuid(), users1.get(0).getGuid());
    }

    @Test(expected = AuthorizableGatewayException.class)
    public void synchronize_createUserInOrganization_notFoundOrg() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);
        List<OrganizationState> organizations = Arrays.asList(new OrganizationState("organization1", "organization1"),
                new OrganizationState("organization2", "organization2"));

        List<UserState> users1 = Arrays.asList(new UserState("user1", "user1"), new UserState("user2", "user2"));
        List<UserState> users2 = Arrays.asList(new UserState("user3", "user3"), new UserState("user4", "user4"));

        organizations.get(0).setUsers(users1);
        organizations.get(1).setUsers(users2);

        // when
        doReturn(organizations).when(cloud).getOrganizations();
        doReturn(false).when(state).getValidState(organizations.get(0).getGuid());

        // then
        engine.synchronizeUser("random", users1.get(0).getGuid());
    }

    @Test(expected = AuthorizableGatewayException.class)
    public void synchronize_createUserInOrganization_notFoundUser() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);
        List<OrganizationState> organizations = Arrays.asList(new OrganizationState("organization1", "organization1"),
                new OrganizationState("organization2", "organization2"));

        List<UserState> users1 = Arrays.asList(new UserState("user1", "user1"), new UserState("user2", "user2"));
        List<UserState> users2 = Arrays.asList(new UserState("user3", "user3"), new UserState("user4", "user4"));

        organizations.get(0).setUsers(users1);
        organizations.get(1).setUsers(users2);

        // when
        doReturn(organizations).when(cloud).getOrganizations();
        doReturn(true).when(state).getValidState(organizations.get(0).getGuid());

        // then
        engine.synchronizeUser(organizations.get(0).getGuid(), "random");
    }

    @Test
    public void state_getInfoAboutAllOrganizations_() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);
        List<OrganizationState> organizations = Arrays.asList(new OrganizationState("organization1", "organization1"),
                new OrganizationState("organization2", "organization2"));

        List<UserState> users1 = Arrays.asList(new UserState("user1", "user1"), new UserState("user2", "user2"));
        List<UserState> users2 = Arrays.asList(new UserState("user3", "user3"), new UserState("user4", "user4"));

        organizations.get(0).setUsers(users1);
        organizations.get(1).setUsers(users2);

        // when
        doReturn(organizations).when(cloud).getOrganizations();

        // then
        PlatformState newOrganizations = engine.state();

        verify(state).getValidState(organizations.get(0).getGuid());
        verify(state).getValidState(organizations.get(0).getGuid(), users1.get(0).getGuid());
        verify(state).getValidState(organizations.get(0).getGuid(), users1.get(1).getGuid());

        verify(state).getValidState(organizations.get(1).getGuid());
        verify(state).getValidState(organizations.get(1).getGuid(), users2.get(0).getGuid());
        verify(state).getValidState(organizations.get(1).getGuid(), users2.get(1).getGuid());

        Assert.assertEquals(newOrganizations.getOrganizationStates(), organizations);
    }

    @Test
    public void state_getInfoAboutOrganization_organizationExists() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);
        List<OrganizationState> organizations = Arrays.asList(new OrganizationState("organization1", "organization1"),
                new OrganizationState("organization2", "organization2"));

        List<UserState> users1 = Arrays.asList(new UserState("user1", "user1"), new UserState("user2", "user2"));
        List<UserState> users2 = Arrays.asList(new UserState("user3", "user3"), new UserState("user4", "user4"));

        organizations.get(0).setUsers(users1);
        organizations.get(1).setUsers(users2);

        // when
        doReturn(organizations).when(cloud).getOrganizations();

        // then
        OrganizationState newOrganization = engine.orgState(organizations.get(0).getGuid());

        verify(state).getValidState(organizations.get(0).getGuid());
        verify(state).getValidState(organizations.get(0).getGuid(), users1.get(0).getGuid());
        verify(state).getValidState(organizations.get(0).getGuid(), users1.get(1).getGuid());

        verify(state, times(0)).getValidState(organizations.get(1).getGuid());
        verify(state, times(0)).getValidState(organizations.get(1).getGuid(), users2.get(0).getGuid());
        verify(state, times(0)).getValidState(organizations.get(1).getGuid(), users2.get(1).getGuid());

        Assert.assertEquals(newOrganization, organizations.get(0));
    }

    @Test(expected = AuthorizableGatewayException.class)
    public void state_getInfoAboutOrganization_organizationDoesntExists() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);
        List<OrganizationState> organizations = Arrays.asList(new OrganizationState("organization1", "organization1"),
                new OrganizationState("organization2", "organization2"));

        List<UserState> users1 = Arrays.asList(new UserState("user1", "user1"), new UserState("user2", "user2"));
        List<UserState> users2 = Arrays.asList(new UserState("user3", "user3"), new UserState("user4", "user4"));

        organizations.get(0).setUsers(users1);
        organizations.get(1).setUsers(users2);

        // when
        doReturn(organizations).when(cloud).getOrganizations();

        // then
        engine.orgState("none");
    }


    @Test
    public void state_getInfoAboutUser_success() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);
        List<OrganizationState> organizations = Arrays.asList(new OrganizationState("organization1", "organization1"),
                new OrganizationState("organization2", "organization2"));

        List<UserState> users1 = Arrays.asList(new UserState("user1", "user1"), new UserState("user2", "user2"));
        List<UserState> users2 = Arrays.asList(new UserState("user3", "user3"), new UserState("user4", "user4"));

        organizations.get(0).setUsers(users1);
        organizations.get(1).setUsers(users2);

        // when
        doReturn(organizations).when(cloud).getOrganizations();

        // then
        UserState newUser = engine.userState(organizations.get(0).getGuid(), users1.get(0).getGuid());

        verify(state, times(0)).getValidState(organizations.get(0).getGuid());
        verify(state).getValidState(organizations.get(0).getGuid(), users1.get(0).getGuid());
        verify(state, times(0)).getValidState(organizations.get(0).getGuid(), users1.get(1).getGuid());

        verify(state, times(0)).getValidState(organizations.get(1).getGuid());
        verify(state, times(0)).getValidState(organizations.get(1).getGuid(), users2.get(0).getGuid());
        verify(state, times(0)).getValidState(organizations.get(1).getGuid(), users2.get(1).getGuid());

        Assert.assertEquals(newUser, users1.get(0));
    }

    @Test(expected = AuthorizableGatewayException.class)
    public void state_getInfoAboutUser_notFoundOrg() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);
        List<OrganizationState> organizations = Arrays.asList(new OrganizationState("organization1", "organization1"),
                new OrganizationState("organization2", "organization2"));

        List<UserState> users1 = Arrays.asList(new UserState("user1", "user1"), new UserState("user2", "user2"));
        List<UserState> users2 = Arrays.asList(new UserState("user3", "user3"), new UserState("user4", "user4"));

        organizations.get(0).setUsers(users1);
        organizations.get(1).setUsers(users2);

        // when
        doReturn(organizations).when(cloud).getOrganizations();

        // then
        engine.userState("random", users1.get(0).getGuid());
    }

    @Test(expected = AuthorizableGatewayException.class)
    public void state_getInfoAboutUser_notFoundUser() throws AuthorizableGatewayException {
        // given
        Engine engine = new Engine(listOfAuthorizables, ENGINE_TIMEOUT_IN_SECONDS, cloud, state);
        List<OrganizationState> organizations = Arrays.asList(new OrganizationState("organization1", "organization1"),
                new OrganizationState("organization2", "organization2"));

        List<UserState> users1 = Arrays.asList(new UserState("user1", "user1"), new UserState("user2", "user2"));
        List<UserState> users2 = Arrays.asList(new UserState("user3", "user3"), new UserState("user4", "user4"));

        organizations.get(0).setUsers(users1);
        organizations.get(1).setUsers(users2);

        // when
        doReturn(organizations).when(cloud).getOrganizations();

        // then
        engine.userState(organizations.get(0).getGuid(), "random");
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

