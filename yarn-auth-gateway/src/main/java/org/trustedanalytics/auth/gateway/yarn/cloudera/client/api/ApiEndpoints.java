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
package org.trustedanalytics.auth.gateway.yarn.cloudera.client.api;

public class ApiEndpoints {
  public static final String CLUSTERS = "/api/v11/clusters";

  public static final String CONFIGURATION =
      "/api/v11/clusters/{clusterName}/services/{service}/config";

  public static final String POOLS_REFRESH =
      "/api/v11/clusters/{clusterName}/commands/poolsRefresh";

  public static final String COMMANDS = "/api/v11/clusters/{clusterName}/commands";

  private ApiEndpoints() {}
}
