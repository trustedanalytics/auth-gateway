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

package org.trustedanalytics.auth.gateway.zookeeper.kerberos;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class KerberosPropertiesTest {

    private static final String KDC = "sample.krb.com";
    private static final String REALM = "SAMPLE_REALM";
    private static final String USER = "username";
    private static final String PASS = "pA$zw0rD";

    @Test
    public void isValid_allPropertiesSet_returnsTrue() throws Exception {
        val krb = new KerberosProperties(KDC, REALM, USER, PASS);
        assertThat(krb.isValid(), equalTo(true));
    }

    @Test
    public void isValid_kdcNull_returnFalse() throws Exception {
        val krb = new KerberosProperties(null, REALM, USER, PASS);
        assertThat(krb.isValid(), equalTo(false));
    }

    @Test
    public void isValid_kdcEmpty_returnFalse() throws Exception {
        val krb = new KerberosProperties("", REALM, USER, PASS);
        assertThat(krb.isValid(), equalTo(false));
    }

    @Test
    public void isValid_realmNull_returnFalse() throws Exception {
        val krb = new KerberosProperties(KDC, null, USER, PASS);
        assertThat(krb.isValid(), equalTo(false));
    }

    @Test
    public void isValid_realmEmpty_returnFalse() throws Exception {
        val krb = new KerberosProperties(KDC, "", USER, PASS);
        assertThat(krb.isValid(), equalTo(false));
    }

    @Test
    public void isValid_userNull_returnFalse() throws Exception {
        val krb = new KerberosProperties(KDC, REALM, null, PASS);
        assertThat(krb.isValid(), equalTo(false));
    }

    @Test
    public void isValid_userEmpty_returnFalse() throws Exception {
        val krb = new KerberosProperties(KDC, REALM, "", PASS);
        assertThat(krb.isValid(), equalTo(false));
    }

    @Test
    public void isValid_passwordNull_returnFalse() throws Exception {
        val krb = new KerberosProperties(KDC, REALM, USER, null);
        assertThat(krb.isValid(), equalTo(false));
    }

    @Test
    public void isValid_passwordEmpty_returnFalse() throws Exception {
        val krb = new KerberosProperties(KDC, REALM, USER, "");
        assertThat(krb.isValid(), equalTo(false));
    }
}
