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
package org.trustedanalytics.auth.gateway.hdfs;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.AclEntry;
import org.apache.hadoop.fs.permission.AclEntryType;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.trustedanalytics.auth.gateway.hdfs.config.ExternalConfiguration;
import org.trustedanalytics.auth.gateway.hdfs.kerberos.KerberosProperties;
import org.trustedanalytics.auth.gateway.hdfs.utils.PathCreator;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

@RunWith(MockitoJUnitRunner.class)
public class HdfsGatewayTest {

  private static final String ORG = "test_org";

  private static final String USER = "test_user";

  private static final Path ORG_PATH = new Path("/org/test_org");

  private static final Path ORG_USERS_PATH = new Path("/org/test_org/user");

  private static final Path TMP_PATH = new Path("/org/test_org/tmp");

  private static final Path BROKER_PATH = new Path("/org/test_org/brokers");

  private static final Path BROKER_METADATA_PATH = new Path("/org/test_org/brokers/metadata");

  private static final Path BROKER_USERSPACE_PATH = new Path("/org/test_org/brokers/userspace");

  private static final Path APP_PATH = new Path("/org/test_org/apps");

  private static final Path USER_PATH = new Path("/org/test_org/user/test_user");

  @Mock
  private HdfsClient hdfsClient;

  @Mock
  private PathCreator pathCreator;

  @Mock
  private KerberosProperties kerberosProperties;

  @Mock
  private ExternalConfiguration externalConfiguration;

  @InjectMocks
  private HdfsGateway hdfsGateway;

  private FsPermission userPermission;

  private FsPermission groupPermission;

  private FsPermission groupExecPermission;

  private List<AclEntry> userAcl;

  @Before
  public void init() throws IOException {
    userPermission = new FsPermission(FsAction.ALL, FsAction.NONE, FsAction.NONE);
    groupPermission = new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.NONE);
    groupExecPermission = new FsPermission(FsAction.ALL, FsAction.EXECUTE, FsAction.NONE);
    HdfsUserPermission test_cfRWX = new HdfsUserPermission("test_cf", FsAction.ALL, AclEntryType.USER);
    HdfsUserPermission test_hiveX = new HdfsUserPermission("test_hive", FsAction.EXECUTE, AclEntryType.USER);
    userAcl = new HdfsClient().getAcl(ImmutableList.of(test_cfRWX, test_hiveX), FsAction.ALL);
    when(pathCreator.createOrgPath("test_org")).thenReturn(ORG_PATH);
    when(pathCreator.createOrgBrokerPath("test_org")).thenReturn(BROKER_PATH);
    when(pathCreator.createBrokerUserspacePath("test_org")).thenReturn(BROKER_USERSPACE_PATH);
    when(pathCreator.createBrokerMetadataPath("test_org")).thenReturn(BROKER_METADATA_PATH);
    when(pathCreator.createOrgTmpPath("test_org")).thenReturn(TMP_PATH);
    when(pathCreator.createOrgAppPath("test_org")).thenReturn(APP_PATH);
    when(pathCreator.createOrgUsersPath("test_org")).thenReturn(ORG_USERS_PATH);
    when(pathCreator.createUserPath("test_org", "test_user")).thenReturn(USER_PATH);
    when(kerberosProperties.getTechnicalPrincipal()).thenReturn("test_cf");
    when(externalConfiguration.getHiveUser()).thenReturn("test_hive");
  }

  @Test
  public void addOrganization_createDirectoryCalled_creationSuccess()
      throws AuthorizableGatewayException, IOException {

    //TODO: remove this line after moving getAcl method to ACLFactory or ACLBuilder
    when(hdfsClient.getAcl(any(List.class), any(FsAction.class))).thenReturn(userAcl);

    hdfsGateway.addOrganization(ORG);
    verify(hdfsClient).createDirectory(ORG_PATH, "test_org_admin", "test_org", groupExecPermission);
    verify(hdfsClient).createDirectory(ORG_USERS_PATH, "test_org_admin", "test_org",
        groupExecPermission);
    verify(hdfsClient).createDirectory(TMP_PATH, "test_org_admin", "test_org", groupPermission);
    verify(hdfsClient).createDirectory(APP_PATH, "test_org_admin", "test_org", groupExecPermission);
    verify(hdfsClient).createDirectory(BROKER_PATH, "test_org_admin", "test_org",
        groupExecPermission);
    verify(hdfsClient).createDirectory(BROKER_USERSPACE_PATH, "test_org_admin", "test_org",
        groupExecPermission);
    verify(hdfsClient).createDirectory(BROKER_METADATA_PATH, "test_org_admin", "test_org",
        groupExecPermission);
    verify(hdfsClient).setACLForDirectory(ORG_PATH, userAcl);
    verify(hdfsClient).setACLForDirectory(BROKER_PATH, userAcl);
    verify(hdfsClient).setACLForDirectory(BROKER_USERSPACE_PATH, userAcl);
    verify(hdfsClient).setACLForDirectory(BROKER_METADATA_PATH, userAcl);
  }

  @Test(expected = AuthorizableGatewayException.class)
  public void addOrganization_hdfsClientThrowIOException_throwAuthorizableGatewayException()
      throws AuthorizableGatewayException, IOException {
    doThrow(new IOException()).when(hdfsClient).createDirectory(ORG_PATH, "test_org_admin",
        "test_org", groupExecPermission);
    hdfsGateway.addOrganization(ORG);
  }

  @Test
  public void removeOrganization_deleteDirectoryCalled_deleteDirectoryMethodCalled()
      throws AuthorizableGatewayException, IOException {
    hdfsGateway.removeOrganization(ORG);
    verify(hdfsClient).deleteDirectory(pathCreator.createOrgPath("test_org"));
  }

  @Test(expected = AuthorizableGatewayException.class)
  public void removeOrganization_hdfsClientThrowIOException_throwAuthorizableGatewayException()
      throws AuthorizableGatewayException, IOException {
    doThrow(new IOException()).when(hdfsClient).deleteDirectory(ORG_PATH);
    hdfsGateway.removeOrganization(ORG);
  }

  @Test
  public void addUserToOrg_createDirectoryCalled_creationSuccess()
      throws AuthorizableGatewayException, IOException {
    hdfsGateway.addUserToOrg(USER, ORG);
    verify(hdfsClient).createDirectory(USER_PATH, "test_user", "test_org", userPermission);
  }

  @Test(expected = AuthorizableGatewayException.class)
  public void addUserToOrg_hdfsClientThrowIOException_throwAuthorizableGatewayException()
      throws AuthorizableGatewayException, IOException {
    doThrow(new IOException()).when(hdfsClient).createDirectory(USER_PATH, "test_user", "test_org",
        userPermission);
    hdfsGateway.addUserToOrg(USER, ORG);
  }

  @Test
  public void removeUserFromOrg_deleteDirectoryCalled_deleteDirectoryMethodCalled()
      throws AuthorizableGatewayException, IOException {
    hdfsGateway.removeUserFromOrg(USER, ORG);
    verify(hdfsClient).deleteDirectory(USER_PATH);
  }

  @Test(expected = AuthorizableGatewayException.class)
  public void removeUserFromOrg_hdfsClientThrowIOException_throwAuthorizableGatewayException()
      throws AuthorizableGatewayException, IOException {
    doThrow(new IOException()).when(hdfsClient).deleteDirectory(USER_PATH);
    hdfsGateway.removeUserFromOrg(USER, ORG);
  }
}
