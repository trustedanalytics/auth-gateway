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

package org.trustedanalytics.auth.gateway.hbase;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.protobuf.generated.AccessControlProtos;
import org.apache.hadoop.hbase.security.access.AccessControlLists;
import org.apache.hadoop.hbase.security.access.Permission;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.ServiceException;

public class HBaseClient {

    private Connection connection;

    public static HBaseClient getNewInstance(Connection connection) {
        return new HBaseClient(connection);
    }

    public HBaseClient(Connection connection) {
        this.connection = connection;
    }

    public void createNamespace(String name) throws IOException {
        NamespaceDescriptor descriptor = NamespaceDescriptor.create(name).build();
        connection.getAdmin().createNamespace(descriptor);
    }

    public boolean checkNamespaceExists(String name) throws IOException{
        for(NamespaceDescriptor namespace:connection.getAdmin().listNamespaceDescriptors())
        {
            if(namespace.getName().equals(name))
                return true;
        }
        return false;
    }

    public void grandPremisionOnNamespace(String user, String namespace, Permission.Action permission) throws IOException, ServiceException {
      try (Table acl = connection.getTable(AccessControlLists.ACL_TABLE_NAME)) {
            BlockingRpcChannel service = acl.coprocessorService(HConstants.EMPTY_START_ROW);
            AccessControlProtos.AccessControlService.BlockingInterface protocol =
                    AccessControlProtos.AccessControlService.newBlockingStub(service);
            ProtobufUtil.grant(protocol, user, namespace, permission);
        }
    }

    public void removeNamespace(String name) throws IOException {
        Pattern allNamespaceTables = Pattern.compile(name.concat(":.*"));
        connection.getAdmin().disableTables(allNamespaceTables);
        connection.getAdmin().deleteTables(allNamespaceTables);
        connection.getAdmin().deleteNamespace(name);
    }
}
