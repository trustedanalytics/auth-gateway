/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trustedanalytics.auth.gateway.zookeeper.client;

public interface ZookeeperClient {
    void createZnode(String znodePath, String content, String username,
        ZookeeperPermission permission) throws Exception;

    void deleteZnode(String znodePath) throws Exception;

    void addUserToAcl(String znodePath, String username, ZookeeperPermission permission)
        throws Exception;

    void removeUserFromAcl(String znodePath, String username) throws Exception;
}
