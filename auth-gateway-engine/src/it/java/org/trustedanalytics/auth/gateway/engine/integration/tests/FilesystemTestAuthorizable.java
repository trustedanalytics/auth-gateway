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
package org.trustedanalytics.auth.gateway.engine.integration.tests;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.trustedanalytics.auth.gateway.spi.Authorizable;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

/*
 * Authorizable that operates on filesystem made for testing purposes.
 */
public class FilesystemTestAuthorizable implements Authorizable {

    private static final String SYSTEM_TEMP = System.getProperty("java.io.tmpdir");

    /**
     * Creates subdirectory in system tmp dir.
     */
    @Override
    public void addOrganization(String orgId, String orgName) throws AuthorizableGatewayException {

        Path dir = FileSystems.getDefault().getPath(SYSTEM_TEMP, orgId);
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            return;
        }
        try {
            Files.createDirectory(dir);
        } catch (IOException e) {
            throw new AuthorizableGatewayException(
                    "Unable to create org directory " + dir.toAbsolutePath(), e);
        }
    }

    @Override
    public void addUser(String userId, String userName) throws AuthorizableGatewayException {

        Path file = FileSystems.getDefault().getPath(SYSTEM_TEMP, userId);
        try {
            Files.createFile(file);
        } catch (IOException e) {
            throw new AuthorizableGatewayException(
                    "Unable to create user file " + file.toAbsolutePath(), e);
        }
    }

    @Override
    public void addUserToOrg(String userId, String orgId) throws AuthorizableGatewayException {
        Path orgDir = FileSystems.getDefault().getPath(SYSTEM_TEMP, orgId);
        Path file = FileSystems.getDefault().getPath(SYSTEM_TEMP, orgId, userId);

        if (!Files.exists(orgDir)) {
            try {
                Files.createDirectory(orgDir);
            } catch (IOException e) {
                throw new AuthorizableGatewayException(
                        "Unable to create org directory " + file.toAbsolutePath(), e);
            }
        }

        try {
            Files.createFile(file);
        } catch (IOException e) {
            throw new AuthorizableGatewayException(
                    "Unable to create user file " + file.toAbsolutePath(), e);
        }
    }

    @Override
    public void removeOrganization(String orgId, String orgName)
            throws AuthorizableGatewayException {
        Path dir = FileSystems.getDefault().getPath(SYSTEM_TEMP, orgId);
        try {
            DirectoryDeleter.deleteDirectoryRecursively(dir);
        } catch (IOException e) {
            throw new AuthorizableGatewayException(
                    "Unable to delete directory " + dir.toAbsolutePath(), e);
        }
    }

    @Override
    public void removeUser(String userId, String userName) throws AuthorizableGatewayException {
        Path file = FileSystems.getDefault().getPath(SYSTEM_TEMP, userId);
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new AuthorizableGatewayException(
                    "Unable to delete user file " + file.toAbsolutePath(), e);
        }
    }

    @Override
    public void removeUserFromOrg(String userId, String orgId) throws AuthorizableGatewayException {
        Path orgDir = FileSystems.getDefault().getPath(SYSTEM_TEMP, orgId);
        Path file = FileSystems.getDefault().getPath(SYSTEM_TEMP, orgId, userId);

        if (!Files.exists(orgDir)) {
            return;
        }

        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new AuthorizableGatewayException(
                    "Unable to delete user file in org" + file.toAbsolutePath(), e);
        }
    }

    @Override
    public String getName() {
        return "FilesystemAuthorizable";
    }
}
