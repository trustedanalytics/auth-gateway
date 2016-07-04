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

import org.trustedanalytics.auth.gateway.spi.Authorizable;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;
import org.trustedanalytics.auth.gateway.state.State;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/*
 * Authorizable that operates on filesystem made for testing purposes.
 */
public class FilesystemTestAuthorizable implements Authorizable {

  private static final String SYSTEM_TEMP = System.getProperty("java.io.tmpdir");

  private static final String ORG_NAME = "test_org";

  private static final String USER_NAME = "test_user";

  private State state;

  public FilesystemTestAuthorizable(State state)
  {
    this.state = state;
  }

  /**
   * Creates subdirectory in system tmp dir.
   */
  @Override
  public void addOrganization(String orgId) throws AuthorizableGatewayException {

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

    state.setValidState(orgId);
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
      throw new AuthorizableGatewayException("Unable to create user file " + file.toAbsolutePath(),
          e);
    }

    state.setValidState(orgId);
    state.setValidState(orgId, userId);
  }

  @Override
  public void removeOrganization(String orgId) throws AuthorizableGatewayException {
    Path dir = FileSystems.getDefault().getPath(SYSTEM_TEMP, orgId);
    try {
      DirectoryDeleter.deleteDirectoryRecursively(dir);
    } catch (IOException e) {
      throw new AuthorizableGatewayException("Unable to delete directory " + dir.toAbsolutePath(),
          e);
    }

    state.unsetValidState(orgId);
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

    state.unsetValidState(orgId);
    state.unsetValidState(orgId, userId);
  }

  @Override
  public void synchronize() throws AuthorizableGatewayException {
    addOrganization(ORG_NAME);
    addUserToOrg(ORG_NAME, USER_NAME);
  }

  @Override
  public String getName() {
    return "FilesystemAuthorizable";
  }
}
