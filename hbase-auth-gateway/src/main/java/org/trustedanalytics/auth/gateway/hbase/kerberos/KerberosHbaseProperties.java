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

package org.trustedanalytics.auth.gateway.hbase.kerberos;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

public final class KerberosHbaseProperties {
    private static final String KDC_PROPERTY = "kdc";
    private static final String REALM_PROPERTY = "krealm";
    private static final String CERT_PROPERTY = "cacert";
    private final String kdc;
    private final String realm;
    private final Map<String, Object> credentials;

    public KerberosHbaseProperties(String kdc, String realm) {
        this.kdc = kdc;
        this.realm = realm;

        this.credentials = ImmutableMap.of("kdc", kdc, "krealm", realm);
    }

    public String getKdc() {
        return this.kdc;
    }

    public String getRealm() {
        return this.realm;
    }

    public Map<String, Object> getCredentials() {
        return this.credentials;
    }
}
