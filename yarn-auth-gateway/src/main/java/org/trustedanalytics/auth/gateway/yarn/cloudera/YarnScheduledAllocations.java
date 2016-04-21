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
package org.trustedanalytics.auth.gateway.yarn.cloudera;

import java.util.List;

import lombok.Getter;
import lombok.ToString;

import org.trustedanalytics.auth.gateway.yarn.PoolConfiguration;
import org.trustedanalytics.auth.gateway.yarn.cloudera.queues.Queue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

@ToString
public final class YarnScheduledAllocations implements PoolConfiguration {

  @Getter
  private final List<Queue> queues;
  @Getter
  private final List<User> users;
  @Getter
  private final List<QueuePlacementRule> queuePlacementRules;
  @Getter
  private final String defaultQueueSchedulingPolicy;
  @Getter
  private final String defaultMinSharePreemptionTimeout;
  @Getter
  private final Integer fairSharePreemptionTimeout;
  @Getter
  private final Integer queueMaxAMShareDefault;
  @Getter
  private final Integer queueMaxAppsDefault;
  @Getter
  private final Integer userMaxAppsDefault;

  @JsonCreator
  private YarnScheduledAllocations(@JsonProperty("users") List<User> users,
      @JsonProperty("queues") List<Queue> queues,
      @JsonProperty("queuePlacementRules") List<QueuePlacementRule> queuePlacementRules,
      @JsonProperty("defaultQueueSchedulingPolicy") String defaultQueueSchedulingPolicy,
      @JsonProperty("defaultMinSharePreemptionTimeout") String defaultMinSharePreemptionTimeout,
      @JsonProperty("fairSharePreemptionTimeout") Integer fairSharePreemptionTimeout,
      @JsonProperty("queueMaxAMShareDefault") Integer queueMaxAMShareDefault,
      @JsonProperty("queueMaxAppsDefault") Integer queueMaxAppsDefault,
      @JsonProperty("userMaxAppsDefault") Integer userMaxAppsDefault) {
    this.users = users;
    this.queues = queues;
    this.queuePlacementRules = queuePlacementRules;
    this.defaultQueueSchedulingPolicy = defaultQueueSchedulingPolicy;
    this.defaultMinSharePreemptionTimeout = defaultMinSharePreemptionTimeout;
    this.fairSharePreemptionTimeout = fairSharePreemptionTimeout;
    this.queueMaxAMShareDefault = queueMaxAMShareDefault;
    this.queueMaxAppsDefault = queueMaxAppsDefault;
    this.userMaxAppsDefault = userMaxAppsDefault;
  }

  public static YarnScheduledAllocations createInstance(YarnScheduledAllocations conf,
      List<Queue> newQueues) {
    return new YarnScheduledAllocations(conf.getUsers(), newQueues, conf.getQueuePlacementRules(),
        conf.getDefaultQueueSchedulingPolicy(), conf.getDefaultMinSharePreemptionTimeout(),
        conf.getFairSharePreemptionTimeout(), conf.getQueueMaxAMShareDefault(),
        conf.getQueueMaxAppsDefault(), conf.getUserMaxAppsDefault());
  }

  @Override
  @JsonIgnore
  public List<Queue> getQueuesList() throws ConfigurationException {
    return getRootQueue().getQueues();
  }

  @Override
  @JsonIgnore
  public Queue getRootQueue() throws ConfigurationException {
    List<Queue> queues = this.getQueues();
    checkQueueSize(queues);
    return queues.get(0);
  }

  private void checkQueueSize(List<Queue> queues) {
    Preconditions.checkArgument(queues.size() > 0);
  }

  public final static class QueuePlacementRule {
    @Getter
    private final String name;
    @Getter
    private final Boolean create;

    @JsonCreator
    public QueuePlacementRule(@JsonProperty("name") String name,
        @JsonProperty("create") Boolean create) {
      this.name = name;
      this.create = create;
    }
  }

  public final static class User {
    @Getter
    private final String name;
    @Getter
    private final Integer maxRunningApps;

    @JsonCreator
    public User(@JsonProperty("name") String name,
        @JsonProperty("maxRunningApps") Integer maxRunningApps) {
      this.name = name;
      this.maxRunningApps = maxRunningApps;
    }
  }

}
