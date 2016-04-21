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

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@ToString
@Builder(builderMethodName = "queueBuilder")
public final class Queue {

  @Getter
  private final String name;
  @Getter
  private final String schedulingPolicy;
  @Getter
  private final String aclSubmitApps;
  @Getter
  private final String aclAdministerApps;
  @Getter
  private final Integer minSharePreemptionTimeout;
  @Getter
  private final List<Queue> queues;
  @Getter
  private final List<SchedulablePropertiesList> schedulablePropertiesList;

  @JsonCreator
  private Queue(
      @JsonProperty("name") String name,
      @JsonProperty("schedulingPolicy") String schedulingPolicy,
      @JsonProperty("aclSubmitApps") String aclSubmitApps,
      @JsonProperty("aclAdministerApps") String aclAdministerApps,
      @JsonProperty("minSharePreemptionTimeout") Integer minSharePreemptionTimeout,
      @JsonProperty("queues") List<Queue> queues,
      @JsonProperty("schedulablePropertiesList") List<SchedulablePropertiesList> schedulablePropertiesList) {
    this.name = name;
    this.schedulingPolicy = schedulingPolicy;
    this.aclSubmitApps = aclSubmitApps;
    this.aclAdministerApps = aclAdministerApps;
    this.minSharePreemptionTimeout = minSharePreemptionTimeout;
    this.queues = queues;
    this.schedulablePropertiesList = schedulablePropertiesList;
  }

  public static Queue createInstance(Queue queue, List<Queue> subQueues) {
    return queueBuilder()
        .name(queue.getName())
        .queues(subQueues)
        .schedulingPolicy(queue.getSchedulingPolicy())
        .aclSubmitApps(queue.getAclSubmitApps())
        .aclAdministerApps(queue.getAclAdministerApps())
        .schedulablePropertiesList(queue.getSchedulablePropertiesList())
        .build();
  }
}
