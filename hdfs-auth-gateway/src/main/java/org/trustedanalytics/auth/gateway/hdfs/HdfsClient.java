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

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.fs.permission.AclEntryScope;
import org.apache.hadoop.fs.permission.AclEntryType;
import org.apache.hadoop.fs.permission.AclEntry;
import org.apache.hadoop.fs.permission.FsAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.auth.gateway.hdfs.config.FileSystemProvider;
import org.trustedanalytics.auth.gateway.hdfs.utils.Qualifiers;

import static java.util.stream.Collectors.toList;

@Profile(Qualifiers.HDFS)
@Configuration
public class HdfsClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(HdfsClient.class);

  @Autowired
  private FileSystemProvider fileSystemProvider;

  public void createDirectory(Path path, String user, String group, FsPermission permission)
      throws IOException {
    FileSystem fileSystem = fileSystemProvider.getFileSystem();
    if (!fileSystem.exists(path)) {
      fileSystem.mkdirs(path);
      fileSystem.setPermission(path, permission);
      fileSystem.setOwner(path, user, group);
    } else {
      LOGGER.warn(String.format("Path already exists: %s", path));
    }
  }

  public void deleteDirectory(Path path) throws IOException {
    FileSystem fileSystem = fileSystemProvider.getFileSystem();
    if (fileSystem.exists(path)) {
      fileSystem.delete(path, true);
    } else {
      LOGGER.warn(String.format("Directory under: %s not exists.", path));
    }
  }

  public void setACLForDirectory(Path path, List<AclEntry> aclEntries) throws IOException {
    FileSystem fileSystem = fileSystemProvider.getFileSystem();
    fileSystem.modifyAclEntries(path, aclEntries);
  }

  //TODO: below method should be moved somewhere (ACLFactory?) or maybe even refactored to builder pattern
  public List<AclEntry> getAcl(List<HdfsUserPermission> acls, FsAction groupAndMaskAction) {
    List<AclEntry> aclList = acls.stream()
        .map(hup -> getAclUserEntry(hup.getUser(), hup.getAction(), hup.getAclEntryType()))
        .collect(toList());
    aclList.add(getAclEntry(groupAndMaskAction, AclEntryType.GROUP));
    aclList.add(getAclEntry(groupAndMaskAction, AclEntryType.MASK));
    return aclList;
  }

  private AclEntry getAclUserEntry(String user, FsAction action, AclEntryType aclEntryType) {
    return new AclEntry.Builder()
        .setName(user)
        .setScope(AclEntryScope.ACCESS)
        .setPermission(action)
        .setType(aclEntryType)
        .build();
  }

  private AclEntry getAclEntry(FsAction action, AclEntryType group) {
    return new AclEntry.Builder()
        .setScope(AclEntryScope.ACCESS)
        .setPermission(action)
        .setType(group)
        .build();
  }
}
