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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.auth.gateway.cloud.Cloud;
import org.trustedanalytics.auth.gateway.engine.response.OrganizationState;
import org.trustedanalytics.auth.gateway.engine.response.PlatformState;
import org.trustedanalytics.auth.gateway.engine.response.UserState;
import org.trustedanalytics.auth.gateway.spi.Authorizable;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;
import org.trustedanalytics.auth.gateway.state.State;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class Engine {

    private static final Logger LOGGER = LoggerFactory.getLogger(Engine.class);

    private List<Authorizable> supportedAuthorizables;
    private long timeoutInSeconds;
    private Cloud cloud;
    private State state;


    public Engine(List<Authorizable> supportedAuthorizables, long timeoutInSeconds, Cloud cloud, State state) {
        this.supportedAuthorizables = supportedAuthorizables;
        this.timeoutInSeconds = timeoutInSeconds;
        this.cloud = cloud;
        this.state=state;
    }

    public PlatformState synchronize() throws AuthorizableGatewayException {
        List<CompletableFuture<Void>> tasks = new LinkedList<>();
        for (Authorizable authorizable : supportedAuthorizables) {
            tasks.add(createFutureForMethod(() -> authorizable.synchronize(), authorizable.getName(),
                    "synchronizing cf"));
        }

        runTasks(tasks, "Error synchronizing cf");

        List<OrganizationState> organizations = cloud.getOrganizations();
        for (OrganizationState organization : organizations) {
            addOrganization(organization.getGuid());
            organization.setSynchronizedState(state.getValidState(organization.getGuid()));
            for (UserState user : organization.getUsers()) {
                addUserToOrg(user.getGuid(), organization.getGuid());
                user.setSynchronizedState(state.getValidState(organization.getGuid(), user.getGuid()));
            }
        }

        LOGGER.info("Synchronize finished.");
        return new PlatformState(organizations);
    }

    public OrganizationState synchronizeOrg(String orgId) throws AuthorizableGatewayException {
        Optional<OrganizationState> organization = cloud.getOrganizations().stream()
                .filter(org -> Objects.equals(org.getGuid(), orgId)).findAny();

        if (!organization.isPresent()) {
            throw new AuthorizableGatewayException("Can not find organization: " + orgId);
        }

        addOrganization(orgId);
        organization.get().setSynchronizedState(state.getValidState(organization.get().getGuid()));
        for (UserState user : organization.get().getUsers()) {
            addUserToOrg(user.getGuid(), organization.get().getGuid());
            user.setSynchronizedState(state.getValidState(organization.get().getGuid(), user.getGuid()));
        }

        LOGGER.info("Synchronize org " + orgId + " finished.");
        return organization.get();
    }

    public UserState synchronizeUser(String orgId, String userId) throws AuthorizableGatewayException {

        Optional<OrganizationState> organization = cloud.getOrganizations().stream()
                .filter(org -> Objects.equals(org.getGuid(), orgId)).findAny();

        if (!organization.isPresent()) {
            throw new AuthorizableGatewayException("Can not find organization: " + orgId);
        }

        organization.get().setSynchronizedState(state.getValidState(organization.get().getGuid()));

        if(!organization.get().isSynchronizedState()) {
            throw new AuthorizableGatewayException("You can not synchronize user in not synchronized org " + orgId);
        }

        Optional<UserState> user = organization.get().getUsers().stream().
                filter(user1 -> Objects.equals(user1.getGuid(), userId)).findAny();

        if (!user.isPresent()) {
            throw new AuthorizableGatewayException(String.format("Can not find user %s in organization %s",
                    userId, orgId));
        }

        addUserToOrg(userId,orgId);
        user.get().setSynchronizedState(state.getValidState(organization.get().getGuid(), user.get().getGuid()));

        return user.get();
    }

    public PlatformState state() throws  AuthorizableGatewayException
    {
        List<OrganizationState> organizations = cloud.getOrganizations();

        for(OrganizationState organization:organizations)
        {
            organization.setSynchronizedState(state.getValidState(organization.getGuid()));

            for(UserState user : organization.getUsers()) {
                user.setSynchronizedState(state.getValidState(organization.getGuid(), user.getGuid()));
            }
        }

        return new PlatformState(organizations);
    }

    public OrganizationState orgState(String orgId) throws  AuthorizableGatewayException
    {
        Optional<OrganizationState> organization = cloud.getOrganizations().stream()
                .filter(org -> Objects.equals(org.getGuid(), orgId)).findAny();

        if (!organization.isPresent()) {
            throw new AuthorizableGatewayException("Can not find organization: " + orgId);
        }

        organization.get().setSynchronizedState(state.getValidState(organization.get().getGuid()));

        for(UserState user : organization.get().getUsers()) {
            user.setSynchronizedState(state.getValidState(organization.get().getGuid(), user.getGuid()));
        }

        return organization.get();
    }

    public UserState userState(String orgId, String userId) throws  AuthorizableGatewayException
    {
        Optional<OrganizationState> organization = cloud.getOrganizations().stream()
                .filter(org -> Objects.equals(org.getGuid(), orgId)).findAny();

        if (!organization.isPresent()) {
            throw new AuthorizableGatewayException("Can not find organization: " + orgId);
        }

        Optional<UserState> user = organization.get().getUsers().stream().
                filter(user1 -> Objects.equals(user1.getGuid(), userId)).findAny();

        if (!user.isPresent()) {
            throw new AuthorizableGatewayException(String.format("Can not find user %s in organization %s",
                    userId, orgId));
        }
        user.get().setSynchronizedState(state.getValidState(organization.get().getGuid(), user.get().getGuid()));

        return user.get();
    }

    public OrganizationState addOrganization(String orgId) throws AuthorizableGatewayException {
        OrganizationState organizationState = orgState(orgId);
        if(organizationState.isSynchronizedState()) {
            LOGGER.info("Organization " + orgId + " is synchronized. Validating organization settings");
        }

        List<CompletableFuture<Void>> tasks = new LinkedList<>();
        for (Authorizable authorizable : supportedAuthorizables) {
            tasks.add(createFutureForMethod(() -> authorizable.addOrganization(orgId),
                    authorizable.getName(), "adding organization"));
        }

        runTasks(tasks, "Error adding organization");
        state.setValidState(orgId);

        return orgState(orgId);
    }

    public UserState addUserToOrg(String userId, String orgId) throws AuthorizableGatewayException {
        UserState userState = userState(orgId, userId);
        if(userState.isSynchronizedState()) {
            LOGGER.info("User " + orgId + "is synchronized. Validating user settings");
        }

        List<CompletableFuture<Void>> tasks = new LinkedList<>();
        for (Authorizable authorizable : supportedAuthorizables) {
            tasks.add(createFutureForMethod(() -> authorizable.addUserToOrg(userId, orgId),
                    authorizable.getName(), "adding user to organization"));
        }

        runTasks(tasks, "Error adding user to organization");
        state.setValidState(orgId, userId);

        return userState(orgId, userId);
    }

    public void removeOrganization(String orgId) throws AuthorizableGatewayException {
        List<CompletableFuture<Void>> tasks = new LinkedList<>();
        for (Authorizable authorizable : supportedAuthorizables) {
            tasks.add(createFutureForMethod(() -> authorizable.removeOrganization(orgId),
                    authorizable.getName(), "removing organization"));
        }
        runTasks(tasks, "Error removing organization");

        state.unsetValidState(orgId);
    }

    public void removeUserFromOrg(String userId, String orgId) throws AuthorizableGatewayException {
        List<CompletableFuture<Void>> tasks = new LinkedList<>();
        for (Authorizable authorizable : supportedAuthorizables) {
            tasks.add(createFutureForMethod(() -> authorizable.removeUserFromOrg(userId, orgId),
                    authorizable.getName(), "removing user from organization"));
        }

        runTasks(tasks, "Error removing user from organization");

        state.unsetValidState(orgId, userId);
    }

    private CompletableFuture<Void> createFutureForMethod(ThrowableAction consumer,
                                                          String authorizableName, String authorizableOperation) {

        return CompletableFuture.completedFuture(null).thenAcceptAsync(x -> {
            try {
                consumer.apply();
                LOGGER.info(authorizableName + " finished " + authorizableOperation);
            } catch (AuthorizableGatewayException e) {
                throw new RuntimeException(authorizableName + " failed: " + e.getMessage(), e);
            }
        });
    }

    private synchronized void runTasks(List<CompletableFuture<Void>> tasks, String errorMessagePrefix)
            throws AuthorizableGatewayException {

        CompletableFuture<Void> allDone =
                CompletableFuture.allOf(tasks.toArray(new CompletableFuture[tasks.size()]));
        try {
            allDone.get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            LOGGER.error(errorMessagePrefix, e);
            throw new AuthorizableGatewayException(errorMessagePrefix + " " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error(errorMessagePrefix, e);
            throw new AuthorizableGatewayException(errorMessagePrefix + " " + e.toString());
        } catch (TimeoutException e) {
            LOGGER.error(errorMessagePrefix, e);
            throw new AuthorizableGatewayException(errorMessagePrefix + " " + e.toString());
        }
    }

}
