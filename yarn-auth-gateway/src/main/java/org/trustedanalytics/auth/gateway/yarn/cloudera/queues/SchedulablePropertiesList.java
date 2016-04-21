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
package org.trustedanalytics.auth.gateway.yarn.cloudera.queues;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder(builderMethodName = "propertiesBuilder")
public final class SchedulablePropertiesList {

  @Getter
  private final Integer weight;
  @Getter
  private final Integer impalaMaxMemory;
  @Getter
  private final Integer impalaMaxQueuedQueries;
  @Getter
  private final Integer impalaMaxRunningQueries;
  @Getter
  private final Integer maxAMShare;
  @Getter
  private final Integer maxRunningApps;
  @Getter
  private final String scheduleName;
  @Getter
  private final Resources maxResources;
  @Getter
  private final Resources minResources;

  @JsonCreator
  public SchedulablePropertiesList(@JsonProperty("weight") Integer weight,
      @JsonProperty("impalaMaxMemory") Integer impalaMaxMemory,
      @JsonProperty("impalaMaxQueuedQueries") Integer impalaMaxQueuedQueries,
      @JsonProperty("impalaMaxRunningQueries") Integer impalaMaxRunningQueries,
      @JsonProperty("maxAmShare") Integer maxAMShare,
      @JsonProperty("maxRunningApps") Integer maxRunningApps,
      @JsonProperty("scheduleName") String scheduleName,
      @JsonProperty("maxResources") Resources maxResources,
      @JsonProperty("minResources") Resources minResources) {
    this.weight = weight;
    this.impalaMaxMemory = impalaMaxMemory;
    this.impalaMaxQueuedQueries = impalaMaxQueuedQueries;
    this.impalaMaxRunningQueries = impalaMaxRunningQueries;
    this.maxAMShare = maxAMShare;
    this.maxRunningApps = maxRunningApps;
    this.scheduleName = scheduleName;
    this.maxResources = maxResources;
    this.minResources = minResources;
  }

  public static SchedulablePropertiesList createInstance(String scheduleName, Integer weight) {
    return propertiesBuilder().scheduleName(scheduleName).weight(weight).build();
  }

  public final static class Resources {

    private final Integer vcores;

    private final Integer memory;

    public Resources(@JsonProperty("name") Integer vcores, @JsonProperty("memory") Integer memory) {
      this.vcores = vcores;
      this.memory = memory;
    }

    public Integer getVcores() {
      return vcores;
    }

    public Integer getMemory() {
      return memory;
    }
  }
}
