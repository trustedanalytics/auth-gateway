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
package org.trustedanalytics.auth.gateway.yarn.cloudera.client.api.entity;

import java.util.List;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Cluster {

  private String name;
  private String displayName;
  private String version;
  private String fullVersion;
  private Boolean maintenanceMode;
  private List<String> maintenanceOwners;
  private String clusterUrl;
  private String hostsUrl;
  private String entityStatus;

  @JsonCreator
  public Cluster(@JsonProperty("name") String name,
      @JsonProperty("displayName") String displayName, @JsonProperty("version") String version,
      @JsonProperty("fullVersion") String fullVersion,
      @JsonProperty("maintenanceMode") Boolean maintenanceMode,
      @JsonProperty("maintenanceOwners") List<String> maintenanceOwners,
      @JsonProperty("clusterUrl") String clusterUrl, @JsonProperty("hostsUrl") String hostsUrl,
      @JsonProperty("entityStatus") String entityStatus) {
    this.name = name;
    this.displayName = displayName;
    this.version = version;
    this.fullVersion = fullVersion;
    this.maintenanceMode = maintenanceMode;
    this.maintenanceOwners = maintenanceOwners;
    this.clusterUrl = clusterUrl;
    this.hostsUrl = hostsUrl;
    this.entityStatus = entityStatus;
  }
}
