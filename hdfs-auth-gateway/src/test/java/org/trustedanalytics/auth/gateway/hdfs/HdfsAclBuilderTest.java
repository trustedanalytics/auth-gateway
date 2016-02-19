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

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.fs.permission.AclEntry;
import org.apache.hadoop.fs.permission.AclEntryScope;
import org.apache.hadoop.fs.permission.AclEntryType;
import org.apache.hadoop.fs.permission.FsAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class HdfsAclBuilderTest {

  private static final String TEST_USER = "test_user";
  private static final String TEST_GROUP = "test_group";
  private static final FsAction TEST_ACTION = FsAction.ALL;

  @Test
  public void createAclListwithUserAclEntry_creationSuccess() {
    List<AclEntry> aclEntries =
        HdfsAclBuilder.newInstance().withUserAclEntry(TEST_USER, TEST_ACTION).build();
    assertThat(aclEntries.size(), equalTo(1));
    assertThatNamedAclEntriesEqual(aclEntries.get(0), TEST_USER, AclEntryType.USER, TEST_ACTION);
  }

  @Test
  public void createAclListwithAclEntry_creationSuccess() {
    List<AclEntry> aclEntries =
        HdfsAclBuilder.newInstance().withAclEntry(TEST_ACTION, AclEntryType.MASK).build();
    assertThat(aclEntries.size(), equalTo(1));
    assertThatAclEntriesEqual(aclEntries.get(0), AclEntryType.MASK, TEST_ACTION);
  }

  @Test
  public void createAclListwithGroupAclEntry_creationSuccess() {
    List<AclEntry> aclEntries =
        HdfsAclBuilder.newInstance().withGroupAclEntry(TEST_GROUP, TEST_ACTION).build();
    assertThat(aclEntries.size(), equalTo(1));
    assertThatNamedAclEntriesEqual(aclEntries.get(0), TEST_GROUP, AclEntryType.GROUP, TEST_ACTION);
  }

  @Test
  public void createAclListWithDefaultEntries_creationSuccess() {
    List<AclEntry> aclEntries =
        HdfsAclBuilder.newInstanceWithDefaultEntries(FsAction.EXECUTE)
            .withUserAclEntry(TEST_USER, TEST_ACTION).build();
    assertThat(aclEntries.size(), equalTo(3));

    List<AclEntryType> aclTypes = aclEntries.stream().map(AclEntry::getType).collect(toList());
    List<FsAction> actionTypes = aclEntries.stream().map(AclEntry::getPermission).collect(toList());

    assertThat(aclTypes,
        containsInAnyOrder(AclEntryType.MASK, AclEntryType.GROUP, AclEntryType.USER));
    assertThat(actionTypes, containsInAnyOrder(FsAction.EXECUTE, FsAction.EXECUTE, FsAction.ALL));
  }

  @Test
  public void createAclListWithUsersAclEntry_withUserActionMap_creationSuccess() {
    ImmutableMap<String, FsAction> usersPermissions =
        ImmutableMap.of("test1", FsAction.NONE, "test2", FsAction.ALL);
    List<AclEntry> aclEntries =
        HdfsAclBuilder.newInstance().withUsersAclEntry(usersPermissions).build();
    assertThat(aclEntries.size(), equalTo(2));
    assertThatNamedAclEntriesEqual(aclEntries.get(0), "test1", AclEntryType.USER, FsAction.NONE);
    assertThatNamedAclEntriesEqual(aclEntries.get(1), "test2", AclEntryType.USER, FsAction.ALL);
  }

  @Test
  public void createAclListWithUsersAclEntry_withUserAction_creationSuccess() {
    List<String> users = Arrays.asList("test1", "test2", "test3");
    List<AclEntry> aclEntries =
        HdfsAclBuilder.newInstance().withUsersAclEntry(users, TEST_ACTION).build();
    assertThat(aclEntries.size(), equalTo(3));
    assertThatNamedAclEntriesEqual(aclEntries.get(0), "test1", AclEntryType.USER, TEST_ACTION);
    assertThatNamedAclEntriesEqual(aclEntries.get(1), "test2", AclEntryType.USER, TEST_ACTION);
    assertThatNamedAclEntriesEqual(aclEntries.get(2), "test3", AclEntryType.USER, TEST_ACTION);
  }

  private void assertThatNamedAclEntriesEqual(AclEntry aclEntry, String name,
      AclEntryType aclEntryType, FsAction action) {
    assertThat(aclEntry.getName(), equalTo(name));
    assertThatAclEntriesEqual(aclEntry, aclEntryType, action);
  }

  private void assertThatAclEntriesEqual(AclEntry aclEntry, AclEntryType aclEntryType,
      FsAction action) {
    assertThat(aclEntry.getType(), equalTo(aclEntryType));
    assertThat(aclEntry.getScope(), equalTo(AclEntryScope.ACCESS));
    assertThat(aclEntry.getPermission(), equalTo(action));
  }

}
