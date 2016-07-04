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
package org.trustedanalytics.auth.gateway.hdfs.integration;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.AclEntry;
import org.apache.hadoop.fs.permission.AclEntryScope;
import org.apache.hadoop.fs.permission.AclEntryType;
import org.apache.hadoop.fs.permission.AclStatus;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.trustedanalytics.auth.gateway.hdfs.HdfsGateway;
import org.trustedanalytics.auth.gateway.hdfs.TestIntegrationApplication;
import org.trustedanalytics.auth.gateway.hdfs.config.FileSystemProvider;
import org.trustedanalytics.auth.gateway.hdfs.integration.config.LocalConfiguration;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({"test", "hdfs-auth-gateway"})
@SpringApplicationConfiguration(classes = {TestIntegrationApplication.class,
        LocalConfiguration.class})
public class HdfsGatewayIntegrationTest {

  @Autowired
  private HdfsGateway hdfsGateway;

  @Autowired
  private FileSystemProvider fileSystemProvider;

  private FileSystem fileSystem;

  private FsPermission userPermission;

  private FsPermission groupPermission;

  private FsPermission groupAllPermission;

  private FsPermission groupReadPermission;

  private static final Path TEST_ORG_ROOT = new Path("/org");

  private static final Path TEST_ORG_PATH = new Path("/org/intel");

  private static final Path TEST_ORG_BROKER_PATH = new Path("/org/intel/brokers");

  private static final Path TEST_ORG_USERSPACE_PATH = new Path("/org/intel/brokers/userspace");

  private static final Path TEST_ORG_USERS_PATH = new Path("/org/intel/user");

  private static final Path TEST_ORG_TMP_PATH = new Path("/org/intel/tmp");

  private static final Path TEST_ORG_SHARED_PATH = new Path("/org/intel/shared");

  private static final Path TEST_ORG_APP_PATH = new Path("/org/intel/apps");

  private static final Path TEST_ORG_OOZIE_PATH = new Path("/org/intel/oozie-jobs");

  private static final Path TEST_ORG_SQOOP_PATH = new Path("/org/intel/sqoop-imports");

  private static final Path TEST_USER_PATH = new Path("/org/intel/user/test_user");

  @Before
  public void init() throws IOException {
    userPermission = new FsPermission(FsAction.ALL, FsAction.NONE, FsAction.NONE);
    groupPermission = new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.NONE);
    groupAllPermission = new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.NONE);
    groupReadPermission = new FsPermission(FsAction.ALL, FsAction.READ_EXECUTE, FsAction.NONE);

    fileSystem = fileSystemProvider.getFileSystem();
    fileSystem.mkdirs(TEST_ORG_ROOT, userPermission);
  }

  @Test
  public void createOrg_createDirectories_directoryExistWithPermissionsAndOwner()
          throws IOException, AuthorizableGatewayException {
    hdfsGateway.addOrganization("intel");

    checkIfDirectoryExistsWithPermissions(TEST_ORG_PATH, "intel_admin", groupAllPermission);
    checkIfDirectoryExistsWithPermissions(TEST_ORG_USERS_PATH, "intel_admin", groupReadPermission);
    checkIfDirectoryExistsWithPermissions(TEST_ORG_TMP_PATH, "intel_admin", groupPermission);
    checkIfDirectoryExistsWithPermissions(TEST_ORG_SHARED_PATH, "intel_admin", groupPermission);
    checkIfDirectoryExistsWithPermissions(TEST_ORG_APP_PATH, "intel_admin", groupReadPermission);
    checkIfDirectoryExistsWithPermissions(TEST_ORG_BROKER_PATH, "intel_admin", groupAllPermission);
    checkIfDirectoryExistsWithPermissions(TEST_ORG_OOZIE_PATH, "intel_admin", groupPermission);
    checkIfDirectoryExistsWithPermissions(TEST_ORG_SQOOP_PATH, "intel_admin", groupPermission);

    checkIfDirectoryExistsWithPermissions(TEST_ORG_USERSPACE_PATH, "intel_admin", groupPermission);

    checkIfDirectoryExistsWithACL(TEST_ORG_PATH, "intel_admin",
            new String[]{"test_cf", "test-vcap", "test-arcadia"},
            new String[]{null, "hive", "intel_sys"});
    checkIfDirectoryExistsWithACL(TEST_ORG_BROKER_PATH, "intel_admin",
            new String[]{"test_cf", "test-vcap", "test-arcadia"},
            new String[]{null, "hive", "intel_sys"});
    checkIfDirectoryExistsWithACL(TEST_ORG_USERSPACE_PATH, "intel_admin",
            new String[]{"test_cf", "test-vcap", "test-arcadia"},
            new String[]{null, "hive", "intel_sys"});
  }

  @Test
  public void createSecondOrgWithSameName_directoryAlreadyExists_doNothing() throws IOException,
          AuthorizableGatewayException {
    hdfsGateway.addOrganization("intel");
    hdfsGateway.addOrganization("intel");
    checkIfDirectoryExistsWithPermissions(TEST_ORG_PATH, "intel_admin", groupAllPermission);
  }

  @Test
  public void createOrgWithUsers_directoryExistWithPermissionsAndOwner_createDirectories()
          throws IOException, AuthorizableGatewayException {
    hdfsGateway.addOrganization("intel");
    checkIfDirectoryExistsWithPermissions(TEST_ORG_PATH, "intel_admin", groupAllPermission);
    hdfsGateway.addUserToOrg("test_user", "intel");
    checkIfDirectoryExistsWithPermissions(TEST_USER_PATH, "test_user", userPermission);
  }

  @Test
  public void createSecondUserWithSameName_createDirectories_doNothing() throws IOException,
          AuthorizableGatewayException {
    hdfsGateway.addOrganization("intel");
    checkIfDirectoryExistsWithPermissions(TEST_ORG_PATH, "intel_admin", groupAllPermission);
    hdfsGateway.addUserToOrg("test_user", "intel");
    hdfsGateway.addUserToOrg("test_user", "intel");
    checkIfDirectoryExistsWithPermissions(TEST_USER_PATH, "test_user", userPermission);
  }

  @Test
  public void deleteEmptyOrg_subDirectoriesNotExists_deleteDirectory() throws IOException,
          AuthorizableGatewayException {
    hdfsGateway.addOrganization("intel");
    checkIfDirectoryExistsWithPermissions(TEST_ORG_PATH, "intel_admin", groupAllPermission);

    hdfsGateway.removeOrganization("intel");
    assertThat(fileSystem.exists(TEST_ORG_PATH), equalTo(false));
  }

  @Test
  public void deleteOrgWithUsers_subDirectoriesExists_deleteDirectoryWithSubDirectories()
          throws IOException, AuthorizableGatewayException {
    hdfsGateway.addOrganization("intel");
    checkIfDirectoryExistsWithPermissions(TEST_ORG_PATH, "intel_admin", groupAllPermission);
    hdfsGateway.addUserToOrg("test_user", "intel");
    checkIfDirectoryExistsWithPermissions(TEST_USER_PATH, "test_user", userPermission);

    hdfsGateway.removeOrganization("intel");

    assertThat(fileSystem.exists(TEST_ORG_PATH), equalTo(false));
    assertThat(fileSystem.exists(TEST_USER_PATH), equalTo(false));
  }

  @Test
  public void deleteNotExistingOrg_directoryNotExists_doNothing() throws IOException,
          AuthorizableGatewayException {
    hdfsGateway.removeOrganization("intel");
    assertThat(fileSystem.exists(TEST_ORG_PATH), equalTo(false));
  }

  private void checkIfDirectoryExistsWithPermissions(Path path, String owner,
                                                     FsPermission permission) throws IOException {
    assertThat(fileSystem.exists(path), equalTo(true));
    assertThat(fileSystem.getFileStatus(path).getOwner(), equalTo(owner));
    assertThat(fileSystem.getFileStatus(path).getPermission(), equalTo(permission));
  }

  private void checkIfDirectoryExistsWithACL(Path path, String owner, String[] privilegedUsers,
                                             String[] privilegedGroups)
          throws IOException {
    AclStatus s = fileSystem.getAclStatus(path);

    assertThat(fileSystem.exists(path), equalTo(true));
    assertThat(s.getOwner(), equalTo(owner));

    List<String> usersWithAcl =
            s.getEntries().stream().filter(entry -> entry.getType().equals(AclEntryType.USER))
                    .filter(entry -> entry.getScope().equals(AclEntryScope.ACCESS))
                    .map(AclEntry::getName).collect(toList());

    List<String> groupsWithAcl =
            s.getEntries().stream().filter(entry -> entry.getType().equals(AclEntryType.GROUP))
                    .filter(entry -> entry.getScope().equals(AclEntryScope.ACCESS))
                    .map(AclEntry::getName).collect(toList());

    List<String> usersWithDefaultAcl =
            s.getEntries().stream().filter(entry -> entry.getType().equals(AclEntryType.USER))
                    .filter(entry -> entry.getScope().equals(AclEntryScope.ACCESS))
                    .map(AclEntry::getName).collect(toList());

    List<String> groupsWithDefaultAcl =
            s.getEntries().stream().filter(entry -> entry.getType().equals(AclEntryType.GROUP))
                    .filter(entry -> entry.getScope().equals(AclEntryScope.ACCESS))
                    .map(AclEntry::getName).collect(toList());

    assertThat(usersWithAcl.size(), equalTo(privilegedUsers.length));
    assertThat(usersWithAcl, containsInAnyOrder(privilegedUsers));

    assertThat(groupsWithAcl.size(), equalTo(privilegedGroups.length));
    assertThat(groupsWithAcl, containsInAnyOrder(privilegedGroups));

    assertThat(usersWithDefaultAcl.size(), equalTo(privilegedUsers.length));
    assertThat(usersWithDefaultAcl, containsInAnyOrder(privilegedUsers));

    assertThat(groupsWithDefaultAcl.size(), equalTo(privilegedGroups.length));
    assertThat(groupsWithDefaultAcl, containsInAnyOrder(privilegedGroups));
  }
}
