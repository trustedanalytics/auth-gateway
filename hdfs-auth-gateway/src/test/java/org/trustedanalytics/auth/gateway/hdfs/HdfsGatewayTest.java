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

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.AclEntry;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.trustedanalytics.auth.gateway.hdfs.config.ExternalConfiguration;
import org.trustedanalytics.auth.gateway.hdfs.config.FileSystemProvider;
import org.trustedanalytics.auth.gateway.hdfs.kerberos.KerberosProperties;
import org.trustedanalytics.auth.gateway.hdfs.utils.PathCreator;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

import java.io.IOException;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HdfsClient.class)
public class HdfsGatewayTest {

  private static final String ORG = "test_org";

  private static final String USER = "test_user";

  private static final String SYS_GROUP = "test_org_sys";

  private static final Path ORG_PATH = new Path("/org/test_org");

  private static final Path ORG_USERS_PATH = new Path("/org/test_org/user");

  private static final Path TMP_PATH = new Path("/org/test_org/tmp");

  private static final Path OOZIE_PATH = new Path("/org/test_org/oozie-jobs");

  private static final Path SQOOP_PATH = new Path("/org/test_org/sqoop-imports");

  private static final Path BROKER_PATH = new Path("/org/test_org/brokers");

  private static final Path BROKER_METADATA_PATH = new Path("/org/test_org/brokers/metadata");

  private static final Path BROKER_USERSPACE_PATH = new Path("/org/test_org/brokers/userspace");

  private static final Path APP_PATH = new Path("/org/test_org/apps");

  private static final Path USER_PATH = new Path("/org/test_org/user/test_user");

  private static final Path USER_HOME_PATH = new Path("/user/test_user");

  @Mock
  private FileSystem fileSystem;

  @Mock
  private FileStatus status;

  @Mock
  private FileSystemProvider fileSystemProvider;

  @Mock
  private HdfsClient hdfsClient;

  @Mock
  private PathCreator pathCreator;

  @Mock
  private KerberosProperties krbProperties;

  @Mock
  private ExternalConfiguration config;

  @InjectMocks
  private HdfsGateway hdfsGateway;

  private FsPermission usrAllGroupAll;

  private FsPermission usrAllGroupExec;

  private FsPermission userAllOnly;

  private FsPermission userAllGroupRead;

  private List<AclEntry> defaultWithTechUserExec;

  private List<AclEntry> defaultWithDefaultTechUserExec;

  private List<AclEntry> defaultWithTechUserAll;

  @Before
  public void init() throws IOException {
    userAllOnly = HdfsPermission.USER_ALL.getPermission();
    usrAllGroupAll = HdfsPermission.USER_ALL_GROUP_ALL.getPermission();
    usrAllGroupExec = HdfsPermission.USER_ALL_GROUP_EXECUTE.getPermission();
    userAllGroupRead = HdfsPermission.USER_ALL_GROUP_READ.getPermission();

    when(fileSystemProvider.getFileSystem()).thenReturn(fileSystem);
    PowerMockito.spy(HdfsClient.class);
    PowerMockito.when(HdfsClient.getNewInstance(fileSystem)).thenReturn(hdfsClient);

    when(pathCreator.getOrgPath("test_org")).thenReturn(ORG_PATH);
    when(pathCreator.getBrokerPath("test_org")).thenReturn(BROKER_PATH);
    when(pathCreator.getUserspacePath("test_org")).thenReturn(BROKER_USERSPACE_PATH);
    when(pathCreator.getTmpPath("test_org")).thenReturn(TMP_PATH);
    when(pathCreator.getAppPath("test_org")).thenReturn(APP_PATH);
    when(pathCreator.getSqoopImportsPath("test_org")).thenReturn(SQOOP_PATH);
    when(pathCreator.getOozieJobsPath("test_org")).thenReturn(OOZIE_PATH);
    when(pathCreator.getUsersPath("test_org")).thenReturn(ORG_USERS_PATH);
    when(pathCreator.getUserPath("test_org", "test_user")).thenReturn(USER_PATH);
    when(pathCreator.getUserHomePath("test_user")).thenReturn(USER_HOME_PATH);
    when(krbProperties.getTechnicalPrincipal()).thenReturn("test_cf");
    when(config.getHiveUser()).thenReturn("test_hive");
    when(config.getArcadiaUser()).thenReturn("test_arcadia");
    when(config.getVcapUser()).thenReturn("test_vcap");

    when(fileSystem.getFileStatus(any())).thenReturn(status);
    when(status.getPermission()).thenReturn(new FsPermission(FsAction.NONE, FsAction.NONE, FsAction.NONE));

    defaultWithTechUserExec =
        hdfsGateway.getDefaultAclWithKrbTechUserAction(FsAction.ALL, FsAction.EXECUTE, SYS_GROUP);
    defaultWithDefaultTechUserExec =
            hdfsGateway.getDefaultAclWithDefaultKrbTechUserAction(FsAction.ALL, FsAction.EXECUTE, SYS_GROUP);
    defaultWithTechUserAll =
        hdfsGateway.getDefaultAclWithKrbTechUserAction(FsAction.ALL, FsAction.ALL, SYS_GROUP);
  }

  @Test
  public void addOrganization_createDirectoryCalled_creationSuccess()
      throws AuthorizableGatewayException, IOException {

    hdfsGateway.addOrganization(ORG);
    verify(hdfsClient).createDirectoryWithAcl(ORG_PATH, "test_org_admin", "test_org",
        userAllGroupRead, defaultWithTechUserExec);
    verify(hdfsClient).createDirectoryWithAcl(BROKER_PATH, "test_org_admin", "test_org",
            usrAllGroupAll, defaultWithDefaultTechUserExec);
    verify(hdfsClient).createDirectoryWithAcl(BROKER_USERSPACE_PATH, "test_org_admin", "test_org",
        userAllGroupRead, defaultWithTechUserAll);
    verify(hdfsClient).createDirectory(ORG_USERS_PATH, "test_org_admin", "test_org",
            userAllGroupRead);
    verify(hdfsClient).createDirectory(TMP_PATH, "test_org_admin", "test_org", usrAllGroupAll);
    verify(hdfsClient).createDirectory(APP_PATH, "test_org_admin", "test_org", userAllGroupRead);
  }

  @Test(expected = AuthorizableGatewayException.class)
  public void addOrganization_hdfsClientThrowIOException_throwAuthorizableGatewayException()
      throws AuthorizableGatewayException, IOException {
    doThrow(new IOException()).when(hdfsClient).createDirectoryWithAcl(ORG_PATH, "test_org_admin",
        "test_org", userAllGroupRead, defaultWithTechUserExec);
    hdfsGateway.addOrganization(ORG);
  }

  @Test
  public void removeOrganization_deleteDirectoryCalled_deleteDirectoryMethodCalled()
      throws AuthorizableGatewayException, IOException {
    hdfsGateway.removeOrganization(ORG);
    verify(hdfsClient).deleteDirectory(pathCreator.getOrgPath("test_org"));
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
    verify(hdfsClient).createDirectory(USER_PATH, "test_user", "test_org", userAllOnly);
    verify(hdfsClient).createDirectory(USER_HOME_PATH, "test_user", "test_user", userAllOnly);
  }

  @Test(expected = AuthorizableGatewayException.class)
  public void addUserToOrg_hdfsClientThrowIOException_throwAuthorizableGatewayException()
      throws AuthorizableGatewayException, IOException {
    doThrow(new IOException()).when(hdfsClient).createDirectory(USER_PATH, "test_user", "test_org",
        userAllOnly);
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
