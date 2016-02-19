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
import org.apache.hadoop.fs.permission.AclEntry;
import org.apache.hadoop.fs.permission.FsPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HdfsClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(HdfsClient.class);

  private final FileSystem fileSystem;

  private HdfsClient(FileSystem fs) {
    this.fileSystem = fs;
  }

  public static HdfsClient getNewInstance(FileSystem fs) {
    return new HdfsClient(fs);
  }

  public void createDirectory(Path path, String user, String group, FsPermission permission)
      throws IOException {
    if (!fileSystem.exists(path)) {
      fileSystem.mkdirs(path);
      fileSystem.setPermission(path, permission);
      fileSystem.setOwner(path, user, group);
    } else {
      LOGGER.warn(String.format("Path already exists: %s", path));
    }
  }

  public void createDirectoryWithAcl(Path path, String user, String group, FsPermission permission, List<AclEntry> aclEntries)
      throws IOException {
    createDirectory(path, user, group, permission);
    setACLForDirectory(path, aclEntries);
  }

  public void deleteDirectory(Path path) throws IOException {
    if (fileSystem.exists(path)) {
      fileSystem.delete(path, true);
    } else {
      LOGGER.warn(String.format("Directory under: %s not exists.", path));
    }
  }

  public void setACLForDirectory(Path path, List<AclEntry> aclEntries) throws IOException {
    fileSystem.modifyAclEntries(path, aclEntries);
  }
}
