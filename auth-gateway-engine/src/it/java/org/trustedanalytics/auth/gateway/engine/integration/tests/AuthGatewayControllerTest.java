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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.trustedanalytics.auth.gateway.Application;
import org.trustedanalytics.auth.gateway.engine.WebApplicationConfig;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class,
                                           TestConfiguration.class,
                                           WebApplicationConfig.class,
                                           OAuth2Helper.class,
                                           AuthorizationServerConfig.class})
@WebAppConfiguration
@IntegrationTest("server.port:0")
@ActiveProfiles("test")
public class AuthGatewayControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private OAuth2Helper helper;

    @Autowired
    @Qualifier("jwtTokenEnhancer")
    private JwtAccessTokenConverter jwtToken;

    @Value("${jwt.token.publicKey}")
    private String publicKey;

    private MockMvc mvc;

    private static final String USER_ID = "666";
    private static final String USER_NAME = "laksj";
    protected static final String ADMIN_NAME = "helmut";
    protected static final String ORG_MANAGER_NAME = "jojo";

    private static final String ORG_ID = "897351";
    private static final String ORG_NAME = "alkheaefg";
    private static final String SYSTEM_TEMP = System.getProperty("java.io.tmpdir");

    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).alwaysDo(print()).build();
        jwtToken.setVerifierKey(publicKey);
        jwtToken.afterPropertiesSet();
        Map<String, JwtAccessTokenConverter> beans = context.getBeansOfType(JwtAccessTokenConverter.class);
        beans.keySet();
    }

    //@Test
    public void doSome() throws Exception {
        mvc.perform(
            put("/organizations/" + ORG_ID + "/users/" + USER_ID)
                .with(helper.bearerToken(ORG_MANAGER_NAME))
        ).andExpect(status().isOk());

        assertTrue(Files.exists(FileSystems.getDefault().getPath(SYSTEM_TEMP, USER_ID)));
    }


    @Test
    public void addUser_shouldReturn200AndCreateFile() throws Exception {
        mvc.perform(
            put("/users/" + USER_ID + "?userName=" + USER_NAME)
                .with(helper.bearerToken(ADMIN_NAME))
        ).andExpect(status().isOk());

        assertTrue(Files.exists(FileSystems.getDefault().getPath(SYSTEM_TEMP, USER_ID)));
    }

    @Test
    public void addOrganizations_shouldReturn200AndCreateDir() throws Exception {
        mvc.perform(
            put("/organizations/" + ORG_ID + "?orgName=" + ORG_NAME)
                .with(helper.bearerToken(ADMIN_NAME))
        ).andExpect(status().isOk());

        assertTrue(Files.exists(FileSystems.getDefault().getPath(SYSTEM_TEMP, ORG_ID)));
        assertTrue(Files.isDirectory(FileSystems.getDefault().getPath(SYSTEM_TEMP, ORG_ID)));
    }

    @Test
    public void addUserToOrg_shouldReturn200AndCreateFileInOrgDir() throws Exception {
        mvc.perform(put("/organizations/" + ORG_ID + "/users/" + USER_ID)
                        .with(helper.bearerToken(ADMIN_NAME))
        ).andExpect(status().isOk());

        assertTrue(Files.exists(FileSystems.getDefault().getPath(SYSTEM_TEMP, ORG_ID, USER_ID)));
    }

    @Test
    public void removeUser_shouldReturn200AndDeleteFileIfExists() throws Exception {
        mvc.perform(delete("/users/" + USER_ID + "?userName=" + USER_NAME)
                        .with(helper.bearerToken(ADMIN_NAME))
        ).andExpect(status().isOk());

        assertFalse(Files.exists(FileSystems.getDefault().getPath(SYSTEM_TEMP, USER_ID)));
    }

    @Test
    public void removeOrganization_shouldReturn200AndDeleteOrgDir() throws Exception {
        mvc.perform(delete("/organizations/" + ORG_ID + "?orgName=" + ORG_NAME)
                        .with(helper.bearerToken(ADMIN_NAME))
        ).andExpect(status().isOk());

        assertFalse(Files.exists(FileSystems.getDefault().getPath(SYSTEM_TEMP, ORG_ID)));
    }

    @Test
    public void removeUserFromOrg_shouldReturn200AndDeleteFileInOrgDir() throws Exception {
        mvc.perform(delete("/organizations/" + ORG_ID + "/users/" + USER_ID)
                        .with(helper.bearerToken(ADMIN_NAME))
        ).andExpect(status().isOk());

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
