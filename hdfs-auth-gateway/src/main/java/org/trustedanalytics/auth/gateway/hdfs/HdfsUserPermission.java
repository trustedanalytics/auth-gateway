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

import org.apache.hadoop.fs.permission.AclEntryType;
import org.apache.hadoop.fs.permission.FsAction;

public class HdfsUserPermission {

    private final String user;
    private final FsAction action;
    private final AclEntryType aclEntryType;

    public HdfsUserPermission(String user, FsAction action, AclEntryType aclEntryType) {
        this.user = user;
        this.action = action;
        this.aclEntryType = aclEntryType;
    }

    public String getUser() {
        return user;
    }

    public FsAction getAction() {
        return action;
    }

    public AclEntryType getAclEntryType() {
        return aclEntryType;
    }
}
