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

package org.trustedanalytics.auth.gateway.zookeeper.client;

class PathOperations {

  private final String parentPath;

  public PathOperations(String parentPath) {
    this.parentPath = parentPath;
  }

  public String makePath(String path) {
    return parentPath + normalizePath(path);
  }

  private static String normalizePath(String path) {
    String normalizedPath = removeTrailingSlashes(removeLeadingSlashes(nullToEmpty(path)));
    return normalizedPath.isEmpty() ? "" : "/" + normalizedPath;
  }

  private static String nullToEmpty(String path) {
    return path == null ? "" : path;
  }

  private static String removeLeadingSlashes(String path) {
    return path.replaceFirst("^/*", "");
  }

  private static String removeTrailingSlashes(String path) {
    return path.replaceFirst("/*$", "");
  }
}
