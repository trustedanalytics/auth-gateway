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
package org.trustedanalytics.auth.gateway.hdfs.utils;

import org.apache.hadoop.fs.Path;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile(Qualifiers.HDFS)
@Configuration
public class PathCreator {

  private static final String ORGS = "org";

  private static final String USER = "user";

  private static final String BROKER = "brokers";

  private static final String BROKER_METADATA = "metadata";

  private static final String BROKER_USERSPACE = "userspace";

  private static final String TMP = "tmp";

  private static final String APP = "apps";

  public Path createOrgBrokerPath(String org) {
    return createPath(ORGS, org, BROKER);
  }

  public Path createBrokerUserspacePath(String org) {
    return createPath(ORGS, org, BROKER, BROKER_USERSPACE);
  }

  public Path createBrokerMetadataPath(String org) {
    return createPath(ORGS, org, BROKER, BROKER_METADATA);
  }

  public Path createOrgTmpPath(String org) {
    return createPath(ORGS, org, TMP);
  }

  public Path createOrgAppPath(String org) {
    return createPath(ORGS, org, APP);
  }

  public Path createOrgPath(String org) {
    return createPath(ORGS, org);
  }

  public Path createOrgUsersPath(String org) {
    return createPath(ORGS, org, USER);
  }

  public Path createUserPath(String org, String user) {
    return createPath(ORGS, org, USER, user);
  }

  private Path createPath(String... args) {
    return getPath(Path.SEPARATOR.concat((String.join(Path.SEPARATOR, args))));
  }

  private Path getPath(String relativePath) {
    return new Path(relativePath);
  }

}
