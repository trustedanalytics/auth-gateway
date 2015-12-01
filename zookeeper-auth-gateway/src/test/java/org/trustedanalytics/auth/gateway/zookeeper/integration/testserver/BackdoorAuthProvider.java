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

package org.trustedanalytics.auth.gateway.zookeeper.integration.testserver;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.ServerCnxn;
import org.apache.zookeeper.server.auth.AuthenticationProvider;

public class BackdoorAuthProvider implements AuthenticationProvider {
    @Override
    public String getScheme() {
        return "backdoor";
    }

    @Override
    public KeeperException.Code handleAuthentication(ServerCnxn serverCnxn, byte[] bytes) {
        serverCnxn.addAuthInfo(new Id("super", ""));
        return KeeperException.Code.OK;
    }

    @Override
    public boolean matches(String s, String s1) {
        return true;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public boolean isValid(String s) {
        return true;
    }
}
