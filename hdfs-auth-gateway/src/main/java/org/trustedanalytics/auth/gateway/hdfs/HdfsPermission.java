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

import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;

public class HdfsPermission {
  public static final HdfsPermission USER_ALL =
      new HdfsPermission(new FsPermission(FsAction.ALL, FsAction.NONE, FsAction.NONE));
  public static final HdfsPermission USER_ALL_GROUP_ALL =
      new HdfsPermission(new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.NONE));
  public static final HdfsPermission USER_ALL_GROUP_EXECUTE =
      new HdfsPermission(new FsPermission(FsAction.ALL, FsAction.EXECUTE, FsAction.NONE));
  public static final HdfsPermission USER_ALL_GROUP_READ =
      new HdfsPermission(new FsPermission(FsAction.ALL, FsAction.READ_EXECUTE, FsAction.NONE));

  private FsPermission permission;

  private HdfsPermission(FsPermission permission) {
    this.permission = permission;
  }

  public FsPermission getPermission() {
    return permission;
  }
}
