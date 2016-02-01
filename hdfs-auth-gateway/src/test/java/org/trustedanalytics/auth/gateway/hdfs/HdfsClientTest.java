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

import com.google.common.collect.ImmutableList;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.auth.gateway.hdfs.config.ExternalConfiguration;
import org.trustedanalytics.auth.gateway.hdfs.config.FileSystemProvider;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HdfsClientTest {

  @Mock
  private FileSystemProvider fileSystemProvider;

  @Mock
  private ExternalConfiguration externalConfiguration;

  @Mock
  private FileSystem fileSystem;

  @InjectMocks
  public HdfsClient hdfsClient;

  private static final Path TEST_PATH = new Path("/org/test");

  private FsPermission userPermission;

  @Before
  public void init() throws IOException {
    userPermission = new FsPermission(FsAction.ALL, FsAction.NONE, FsAction.NONE);
    when(fileSystemProvider.getFileSystem()).thenReturn(fileSystem);
  }

  @Test
  public void createDirectory_fileSystemMkdirsAndSetOwnerCalled_creationSuccess()
      throws IOException {
    when(fileSystem.exists(TEST_PATH)).thenReturn(false);

    hdfsClient.createDirectory(TEST_PATH, "test_admin", "test", userPermission);

    verify(fileSystem).exists(TEST_PATH);
    verify(fileSystem).mkdirs(TEST_PATH);
    verify(fileSystem).setPermission(TEST_PATH, userPermission);
    verify(fileSystem).setOwner(TEST_PATH, "test_admin", "test");
    verify(fileSystemProvider).getFileSystem();
  }

  @Test()
  public void createDirectory_directoryAlreadyExists_doNothing() throws IOException {
    when(fileSystem.exists(TEST_PATH)).thenReturn(true);

    hdfsClient.createDirectory(TEST_PATH, "test_admin", "test", userPermission);
    verify(fileSystem, times(0)).mkdirs(TEST_PATH);
    verify(fileSystem, times(0)).setPermission(TEST_PATH, userPermission);
    verify(fileSystem, times(0)).setOwner(TEST_PATH, "test_admin", "test");
    verify(fileSystemProvider).getFileSystem();
  }

  @Test
  public void deleteDirectory_fileSystemDeleteCalled_deletionSuccess() throws IOException {
    when(fileSystem.exists(TEST_PATH)).thenReturn(true);

    hdfsClient.deleteDirectory(TEST_PATH);

    verify(fileSystem).exists(TEST_PATH);
    verify(fileSystem).delete(TEST_PATH, true);
    verify(fileSystemProvider).getFileSystem();
  }

  @Test
  public void deleteDirectory_directoryNotExists_doNothing() throws IOException {
    when(fileSystem.exists(TEST_PATH)).thenReturn(false);

    hdfsClient.deleteDirectory(TEST_PATH);
    verify(fileSystem, times(0)).mkdirs(TEST_PATH, userPermission);
    verify(fileSystem, times(0)).setOwner(TEST_PATH, "test_admin", "test");
    verify(fileSystemProvider).getFileSystem();
  }

  @Test
  public void getAcl_getAclCalled_aclEntryCreated() throws IOException {
    List<AclEntry> userAcl = hdfsClient.getAcl(
        ImmutableList.of(
            new HdfsUserPermission("test_user", FsAction.ALL, AclEntryType.USER),
            new HdfsUserPermission("test_user2", FsAction.READ_EXECUTE, AclEntryType.USER)),
        FsAction.ALL);

    assertUserAcl(userAcl.get(0), "test_user", FsAction.ALL, AclEntryType.USER);
    assertUserAcl(userAcl.get(1), "test_user2", FsAction.READ_EXECUTE, AclEntryType.USER);
    assertUserAcl(userAcl.get(2), FsAction.ALL, AclEntryType.GROUP);
    assertUserAcl(userAcl.get(3), FsAction.ALL, AclEntryType.MASK);
  }

  private void assertUserAcl(AclEntry aclEntry, String name, FsAction fsAction, AclEntryType entryType) {
    assertUserAcl(aclEntry, fsAction, entryType);
    assertThat(aclEntry.getName(), equalTo(name));
  }

  private void assertUserAcl(AclEntry aclEntry, FsAction fsAction, AclEntryType entryType) {
    assertThat(aclEntry.getPermission(), equalTo(fsAction));
    assertThat(aclEntry.getType(), equalTo(entryType));
  }
}
