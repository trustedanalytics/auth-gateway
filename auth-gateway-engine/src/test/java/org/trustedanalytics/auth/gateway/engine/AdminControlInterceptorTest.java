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
package org.trustedanalytics.auth.gateway.engine;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class AdminControlInterceptorTest {

  @Mock
  private HttpServletRequest httpRequest;

  @Mock
  private HttpServletResponse httpResponse;

  public static final String
      UBER_ADMIN_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiIxZjZmNTI1ZS1iZDc1LTRiNzYtYmExNi"
                         + "05NzJkZDkzNGMxNWQiLCJzdWIiOiI5N2M0YjcxMS03YzNiLTQ1ZjctYjA2ZS1"
                         + "jNjcxYzU5MTViMjciLCJzY29wZSI6WyJzY2ltLnJlYWQiLCJjb25zb2xlLmFk"
                         + "bWluIiwiY2xvdWRfY29udHJvbGxlci5hZG1pbiIsInBhc3N3b3JkLndyaXRlI"
                         + "iwic2NpbS53cml0ZSIsIm9wZW5pZCIsImNsb3VkX2NvbnRyb2xsZXIud3JpdG"
                         + "UiLCJjbG91ZF9jb250cm9sbGVyLnJlYWQiLCJkb3BwbGVyLmZpcmVob3NlIl0"
                         + "sImNsaWVudF9pZCI6ImNmIiwiY2lkIjoiY2YiLCJhenAiOiJjZiIsImdyYW50"
                         + "X3R5cGUiOiJwYXNzd29yZCIsInVzZXJfaWQiOiI5N2M0YjcxMS03YzNiLTQ1Z"
                         + "jctYjA2ZS1jNjcxYzU5MTViMjciLCJ1c2VyX25hbWUiOiJhZG1pbiIsImVtYW"
                         + "lsIjoiYWRtaW4iLCJyZXZfc2lnIjoiYTFhMjRiNCIsImlhdCI6MTQ1NzY4OTc"
                         + "xMiwiZXhwIjoxNDU3NjkwMzEyLCJpc3MiOiJodHRwczovL3VhYS5qYW4tMy1r"
                         + "cmItZmluYWwuZ290YXBhYXMuZXUvb2F1dGgvdG9rZW4iLCJ6aWQiOiJ1YWEiL"
                         + "CJhdWQiOlsiZG9wcGxlciIsInNjaW0iLCJjb25zb2xlIiwib3BlbmlkIiwiY2"
                         + "xvdWRfY29udHJvbGxlciIsInBhc3N3b3JkIiwiY2YiXX0.syY47zrhb80J7zV"
                         + "Qr516WRAOefJ9cgDCiI8Rg4ius3gCktYa8piA4EtcCxpOGyxaIXt2e_do0zLt"
                         + "2RgXo2vNX7q5_5X5GXFs2HrmB6vnBqApO1ZHiYk2Kck1hX6MMABkmOaMWueuU"
                         + "54TcpdKSiHl0JCUFqStUjxz06SooArh5qvdEmyKa619GpL4JhKHLnSuw4Plof"
                         + "-XVqKbdS01qliy8qOHfneNpp22PZBh1e4axjNTViLb4xThueGMDWkZ49W8tEd"
                         + "-40pz_rBb78mmTzVVNhCbaG5ZdLqsjQ2nFUe1uLy-NKQgXi-BareUltem9NN-"
                         + "ycTEWWWVMgtGkLOLQW-IYQ";

  private static final String
      ORG_MANAGER_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiIwMTMyYWQ4Ni1iZTFhLTRkYTYtYjg0Yi"
                          + "0wYjUxODk1M2IwNzgiLCJzdWIiOiJlZGRhMjExOC0yOTkxLTQ4MjYtYWUyZi0"
                          + "0OGUxZTk3MmYyNGQiLCJzY29wZSI6WyJwYXNzd29yZC53cml0ZSIsIm9wZW5p"
                          + "ZCIsImNsb3VkX2NvbnRyb2xsZXIud3JpdGUiLCJjbG91ZF9jb250cm9sbGVyL"
                          + "nJlYWQiXSwiY2xpZW50X2lkIjoiY2YiLCJjaWQiOiJjZiIsImF6cCI6ImNmIi"
                          + "wiZ3JhbnRfdHlwZSI6InBhc3N3b3JkIiwidXNlcl9pZCI6ImVkZGEyMTE4LTI"
                          + "5OTEtNDgyNi1hZTJmLTQ4ZTFlOTcyZjI0ZCIsInVzZXJfbmFtZSI6ImFydHVy"
                          + "IiwiZW1haWwiOiJhcnR1ciIsInJldl9zaWciOiIyN2NkZTliYiIsImlhdCI6M"
                          + "TQ1NzcwMTYxMiwiZXhwIjoxNDU3NzAyMjEyLCJpc3MiOiJodHRwczovL3VhYS"
                          + "5qYW4tMy1rcmItZmluYWwuZ290YXBhYXMuZXUvb2F1dGgvdG9rZW4iLCJ6aWQ"
                          + "iOiJ1YWEiLCJhdWQiOlsib3BlbmlkIiwiY2xvdWRfY29udHJvbGxlciIsInBh"
                          + "c3N3b3JkIiwiY2YiXX0.fzlBHYQlHp9Sr7NPyxJ0Zb35BT9_GmuVeczA7CJaT"
                          + "nMZU6wTJj9ap_QdEIiSrHvL6Mzk5O5X6-oPIlzW5E3t0JAP0xVicDGpg6tXn3"
                          + "W3dWjxtNBWUEsm5SUe4al6VO8NdhBkKsyEeNEvRSncV-7jrKk9Xwsvo5hrVpD"
                          + "M5TEe_qPwIAGFiaKMXwa3lhnaGONObivkTtccUvOk8_UvwXI647LBmdvxUaMf"
                          + "W3qhLUxHJe7SjdlGmR9SzWLUSUcF5AXHIkIZwh4JAV1n3ty4Q85JAobwjZwAV"
                          + "pGLcUsBFWDAlKb183Pj0ZmeBuYLANohjkb9tTl4LGhLlYV9TqLadtl2mA";

  @Test
  public void testPreHandle_userAdmin_returnTrue() throws Exception {
    AdminControlInterceptor toTest = new AdminControlInterceptor(() -> UBER_ADMIN_TOKEN);
    Assert.assertTrue(toTest.preHandle(httpRequest, httpResponse, new Object()));
  }

  @Test
  public void testPreHandle_orgManager_returnFalse() throws Exception {
    AdminControlInterceptor toTest = new AdminControlInterceptor(() -> ORG_MANAGER_TOKEN);
    Assert.assertFalse(toTest.preHandle(httpRequest, httpResponse, new Object()));
  }

}
