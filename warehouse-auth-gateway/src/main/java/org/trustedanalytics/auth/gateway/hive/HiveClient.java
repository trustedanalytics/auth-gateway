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
package org.trustedanalytics.auth.gateway.hive;

import java.io.Closeable;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

public class HiveClient implements Closeable {

  private static final String JDBC_DRIVER = "org.apache.hive.jdbc.HiveDriver";

  private static final Logger LOGGER = LoggerFactory.getLogger(HiveClient.class);

  private Connection client;

  private String connectionUrl;
  private String hdfsUri;
  private UserGroupInformation ugi;


  public HiveClient(Builder builder) throws IOException, InterruptedException {
    this.connectionUrl = builder.getConnectionUrl();
    this.hdfsUri = builder.getHdfsUri();
    this.ugi = builder.getUgi();

    client = (Connection) ugi.doAs((PrivilegedExceptionAction<Object>) () -> {
      Class.forName(JDBC_DRIVER);
      return DriverManager.getConnection(connectionUrl, null, null);
    });
  }

  @Override
  public void close() throws IOException {
    try {
      if (client != null && !client.isClosed()) {
        client.close();
      }
    } catch (SQLException e) {
      throw new IOException(e);
    }
  }

  public synchronized void createDatabase(String databaseName) throws AuthorizableGatewayException {

    String normalizedDatabaseName = databaseName.replace('-', '_');
    try (Statement statement = client.createStatement()) {
      statement
          .execute(String.format("CREATE DATABASE IF NOT EXISTS `%s`", normalizedDatabaseName));
      LOGGER.info(String.format("Database %s created", normalizedDatabaseName));
    } catch (SQLException sqlException) {
      throw new AuthorizableGatewayException(
          "SQL create database query failed for database: " + normalizedDatabaseName, sqlException);
    }
  }

  public synchronized void grantPrivileges(String databaseName, String roleName)
      throws AuthorizableGatewayException {

    String normalizedDatabaseName = databaseName.replace('-', '_');
    try (Statement statement = client.createStatement()) {
        statement.execute(String.format("GRANT ALL ON DATABASE `%s` TO ROLE `%s`",
            normalizedDatabaseName, roleName));
      LOGGER.info(String.format("Access for role %s to database %s granted", roleName,
          normalizedDatabaseName));
    } catch (SQLException sqlException) {
      throw new AuthorizableGatewayException(
          "SQL grant privileges query failed for database: " + normalizedDatabaseName,
          sqlException);
    }
  }

  public synchronized void grantPrivilegesForUri(String orgId, String roleName)
      throws AuthorizableGatewayException {
    String uri = hdfsUri + orgId;
    try (Statement statement = client.createStatement()) {
      statement.execute(String.format("GRANT ALL ON URI \"%s\" TO ROLE `%s`", uri, roleName));
      LOGGER.info(String.format("Access for role %s to uri %s granted", roleName, uri));
    } catch (SQLException sqlException) {
      throw new AuthorizableGatewayException("SQL grant privileges query failed for uri: " + uri,
          sqlException);
    }
  }

  static class Builder {

    private String connectionUrl;
    private String hdfsUri;
    private UserGroupInformation ugi;

    Builder(UserGroupInformation ugi) {
      this.ugi = ugi;
    }

    public Builder connectionUrl(String connectionUrl) {
      this.connectionUrl = connectionUrl;
      return this;
    }

    public Builder hdfsUri(String hdfsUri) {
      this.hdfsUri = hdfsUri;
      return this;
    }

    String getConnectionUrl() {
      return connectionUrl;
    }

    String getHdfsUri() {
      return hdfsUri;
    }

    UserGroupInformation getUgi() {
      return ugi;
    }

    HiveClient build() throws IOException, InterruptedException {
      return new HiveClient(this);
    }

  }
}
