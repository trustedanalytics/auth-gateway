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
package org.trustedanalytics.auth.gateway.impala;

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

public class ImpalaClient implements Closeable {

  private static final String JDBC_DRIVER = "org.apache.hive.jdbc.HiveDriver";

  private static final Logger LOGGER = LoggerFactory.getLogger(ImpalaClient.class);

  private Connection client;

  private String connectionUrl;
  private boolean available;
  private UserGroupInformation ugi;


  public ImpalaClient(Builder builder) throws IOException, InterruptedException {
    this.connectionUrl = builder.getConnectionUrl();
    this.available = builder.getAvailable();
    this.ugi = builder.getUgi();

    if (available) {
      client = (Connection) ugi.doAs((PrivilegedExceptionAction<Object>) () -> {
        Class.forName(JDBC_DRIVER);
        return DriverManager.getConnection(connectionUrl, null, null);
      });
    }
  }

  public boolean isAvailable() { return available; }

  public void close() throws IOException {
    try {
      if (client != null && !client.isClosed()) {
        client.close();
      }
    } catch (SQLException e) {
      throw new IOException(e);
    }
  }

  public synchronized void invalidateMetadata() throws AuthorizableGatewayException {
    if(isAvailable()) {
      LOGGER.info("Refresh impala cache");
      try (Statement statement = client.createStatement()) {
        statement.execute(String.format("INVALIDATE METADATA"));
      } catch (SQLException sqlException) {
        throw new AuthorizableGatewayException("SQL invalidate metadata query failed" ,
            sqlException);
      }
    }
  }

  static class Builder {

    private String connectionUrl;
    private  boolean available;
    private UserGroupInformation ugi;

    Builder(UserGroupInformation ugi) {
      this.ugi = ugi;
    }

    public Builder connectionUrl(String connectionUrl) {
      this.connectionUrl = connectionUrl;
      return this;
    }

    public Builder available(boolean available)
    {
      this.available = available;
      return this;
    }

    String getConnectionUrl() {
      return connectionUrl;
    }

    boolean getAvailable() { return available; }

    UserGroupInformation getUgi() {
      return ugi;
    }

    ImpalaClient build() throws IOException, InterruptedException {
      return new ImpalaClient(this);
    }

  }
}
