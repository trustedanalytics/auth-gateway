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

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.trustedanalytics.auth.gateway.Application;

import com.jayway.restassured.RestAssured;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class, TestConfiguration.class})
@WebAppConfiguration
@IntegrationTest("server.port:0")
@ActiveProfiles("test")
public class AuthGatewayControllerTest {

    @Value("${local.server.port}")
    int port;

    private static final String USER_ID = "666";
    private static final String USER_NAME = "laksj";
    private static final String ORG_ID = "897351";
    private static final String ORG_NAME = "alkheaefg";
    private static final String SYSTEM_TEMP = System.getProperty("java.io.tmpdir");

    @Before
    public void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void addUser_shouldReturn200AndCreateFile() {
        given().when().put("/users/" + USER_ID + "?userName=" + USER_NAME).then().assertThat()
                .statusCode(HttpStatus.OK.value());

        assertTrue(Files.exists(FileSystems.getDefault().getPath(SYSTEM_TEMP, USER_ID)));
    }

    @Test
    public void addOrganizations_shouldReturn200AndCreateDir() {
        given().when().put("/organizations/" + ORG_ID + "?orgName=" + ORG_NAME).then().assertThat()
                .statusCode(HttpStatus.OK.value());
        
        assertTrue(Files.exists(FileSystems.getDefault().getPath(SYSTEM_TEMP, ORG_ID)));
        assertTrue(Files.isDirectory(FileSystems.getDefault().getPath(SYSTEM_TEMP, ORG_ID)));
    }

    @Test
    public void addUserToOrg_shouldReturn200AndCreateFileInOrgDir() {
        given().when().put("/organizations/" + ORG_ID + "/users/" + USER_ID).then().assertThat()
                .statusCode(HttpStatus.OK.value());
        
        assertTrue(Files.exists(FileSystems.getDefault().getPath(SYSTEM_TEMP, ORG_ID, USER_ID)));
    }

    @Test
    public void removeUser_shouldReturn200AndDeleteFileIfExists() {
        given().when().delete("/users/" + USER_ID + "?userName=" + USER_NAME).then().assertThat()
                .statusCode(HttpStatus.OK.value());
        
        assertFalse(Files.exists(FileSystems.getDefault().getPath(SYSTEM_TEMP, USER_ID)));
    }

    @Test
    public void removeOrganization_shouldReturn200AndDeleteOrgDir() {
        given().when().delete("/organizations/" + ORG_ID + "?orgName=" + ORG_NAME).then()
                .assertThat().statusCode(HttpStatus.OK.value());
        
        assertFalse(Files.exists(FileSystems.getDefault().getPath(SYSTEM_TEMP, ORG_ID)));
    }

    @Test
    public void removeUserFromOrg_shouldReturn200AndDeleteFileInOrgDir() {
        given().when().delete("/organizations/" + ORG_ID + "/users/" + USER_ID).then().assertThat()
                .statusCode(HttpStatus.OK.value());
        
        assertFalse(Files.exists(FileSystems.getDefault().getPath(SYSTEM_TEMP, ORG_ID, USER_ID)));
    }

    @After
    public void cleanUp() {
        Path userFile = FileSystems.getDefault().getPath(SYSTEM_TEMP, USER_ID);
        Path orgDir = FileSystems.getDefault().getPath(SYSTEM_TEMP, ORG_ID);

        try {
            Files.delete(userFile);
            DirectoryDeleter.deleteDirectoryRecursively(orgDir);
        } catch (IOException e) {
        }
    }

}
