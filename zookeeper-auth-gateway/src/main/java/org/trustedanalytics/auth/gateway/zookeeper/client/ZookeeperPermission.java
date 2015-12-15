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

package org.trustedanalytics.auth.gateway.zookeeper.client;

import org.apache.zookeeper.ZooDefs;

/**
 * Zookeeper access levels: C - create R - read D - delete W - write A - admin
 *
 * e.g. CRDW is a union of create, read, delete and write
 */
public enum ZookeeperPermission {

  // create read delete write
  CRDW(ZooDefs.Perms.ALL - ZooDefs.Perms.ADMIN),

  // create read delete write admin
  CRDWA(ZooDefs.Perms.ALL);

  private int perms;

  ZookeeperPermission(int perms) {
    this.perms = perms;
  }

  public int getPerms() {
    return perms;
  }
}
