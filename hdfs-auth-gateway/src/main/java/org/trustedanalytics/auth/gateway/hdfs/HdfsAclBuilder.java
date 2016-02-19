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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.permission.AclEntry;
import org.apache.hadoop.fs.permission.AclEntryScope;
import org.apache.hadoop.fs.permission.AclEntryType;
import org.apache.hadoop.fs.permission.FsAction;

import com.google.common.base.Preconditions;

final class HdfsAclBuilder {

  private List<AclEntry> aclEntryList;

  private HdfsAclBuilder() {
    aclEntryList = new ArrayList<>();
  }

  public HdfsAclBuilder withUsersAclEntry(Map<String, FsAction> usersAclEntry) {
    usersAclEntry
        .entrySet()
        .stream()
        .forEach(
            entry -> aclEntryList.add(getNamedAclEntry(entry.getKey(), entry.getValue(),
                AclEntryType.USER)));
    return this;
  }

  public HdfsAclBuilder withUsersAclEntry(List<String> users, FsAction fsAction) {
    users.stream().forEach(
        user -> aclEntryList.add(getNamedAclEntry(user, fsAction, AclEntryType.USER)));
    return this;
  }

  public HdfsAclBuilder withUserAclEntry(String user, FsAction fsAction) {
    aclEntryList.add(getNamedAclEntry(user, fsAction, AclEntryType.USER));
    return this;
  }

  public HdfsAclBuilder withGroupAclEntry(String user, FsAction fsAction) {
    aclEntryList.add(getNamedAclEntry(user, fsAction, AclEntryType.GROUP));
    return this;
  }

  public HdfsAclBuilder withAclEntry(FsAction action, AclEntryType aclEntry) {
    aclEntryList.add(getAclEntry(action, aclEntry));
    return this;
  }

  public List<AclEntry> build() {
    Preconditions.checkArgument(!this.aclEntryList.isEmpty(), "Created Entry list cannot be null");
    return this.aclEntryList;
  }

  private AclEntry getNamedAclEntry(String user, FsAction action, AclEntryType aclEntryType) {
    return new AclEntry.Builder().setName(user).setScope(AclEntryScope.ACCESS)
        .setPermission(action).setType(aclEntryType).build();
  }

  private AclEntry getAclEntry(FsAction action, AclEntryType group) {
    return new AclEntry.Builder().setScope(AclEntryScope.ACCESS).setPermission(action)
        .setType(group).build();
  }

  public static HdfsAclBuilder newInstance() {
    return new HdfsAclBuilder();
  }

  public static HdfsAclBuilder newInstanceWithDefaultEntries(FsAction groupAndMaskAction) {
    return new HdfsAclBuilder().withAclEntry(groupAndMaskAction, AclEntryType.GROUP).withAclEntry(
        groupAndMaskAction, AclEntryType.MASK);
  }
}
