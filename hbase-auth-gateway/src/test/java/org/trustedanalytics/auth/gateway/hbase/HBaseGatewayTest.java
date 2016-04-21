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

import com.google.protobuf.ServiceException;
import org.apache.hadoop.hbase.NamespaceExistException;
import org.apache.hadoop.hbase.NamespaceNotFoundException;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.security.access.Permission;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

import java.io.IOException;

import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HBaseClient.class)
public class HBaseGatewayTest {

    private static final String ORG = "test-org-id";

    private static final String ORG_NAMESPACE = "testorgid";

    private static final String ORG_GROUP = "@test-org-id";

    @Mock
    private Connection connection;

    @Mock
    private HBaseClient hBaseClient;

    @InjectMocks
    private HBaseGateway hBaseGateway;

    @Before
    public void init() throws IOException {
        PowerMockito.spy(HBaseClient.class);
        PowerMockito.when(HBaseClient.getNewInstance(connection)).thenReturn(hBaseClient);
    }

    @Test
    public void addOrganization_creationSuccess() throws AuthorizableGatewayException, IOException, ServiceException {
        hBaseGateway.addOrganization(ORG);

        Mockito.verify(hBaseClient).createNamespace(ORG_NAMESPACE);
        Mockito.verify(hBaseClient).grandPremisionOnNamespace(ORG_GROUP, ORG_NAMESPACE, Permission.Action.CREATE);
    }

    @Test
    public void addOrganization_createNamespaceThrowNamespaceExistException()
            throws AuthorizableGatewayException, IOException, ServiceException {

        doThrow(new NamespaceExistException()).when(hBaseClient).createNamespace(ORG_NAMESPACE);

        hBaseGateway.addOrganization(ORG);

        Mockito.verify(hBaseClient).createNamespace(ORG_NAMESPACE);
        Mockito.verify(hBaseClient).grandPremisionOnNamespace(ORG_GROUP, ORG_NAMESPACE, Permission.Action.CREATE);
    }

    @Test(expected = AuthorizableGatewayException.class)
    public void addOrganization_createNamespaceThrowIOException()
            throws AuthorizableGatewayException, IOException, ServiceException {

        doThrow(new IOException()).when(hBaseClient).createNamespace(ORG_NAMESPACE);

        hBaseGateway.addOrganization(ORG);
    }

    @Test(expected = AuthorizableGatewayException.class)
    public void addOrganization_grandPremisionOnNamespaceThrowServiceException()
            throws AuthorizableGatewayException, IOException, ServiceException {

        doThrow(new ServiceException("")).when(hBaseClient).grandPremisionOnNamespace(
                ORG_GROUP, ORG_NAMESPACE, Permission.Action.CREATE);

        hBaseGateway.addOrganization(ORG);
    }

    @Test
    public void removeOrganization_Success() throws AuthorizableGatewayException, IOException {
        hBaseGateway.removeOrganization(ORG);

        Mockito.verify(hBaseClient).removeNamespace(ORG_NAMESPACE);
    }

    @Test
    public void removeOrganization_removeNamespaceThrowNamespaceNotFoundException()
            throws AuthorizableGatewayException, IOException {
        doThrow(new NamespaceNotFoundException()).when(hBaseClient).removeNamespace(ORG_NAMESPACE);

        hBaseGateway.removeOrganization(ORG);

        Mockito.verify(hBaseClient).removeNamespace(ORG_NAMESPACE);
    }

    @Test(expected = AuthorizableGatewayException.class)
    public void removeOrganization_removeNamespaceThrowIOException()
            throws AuthorizableGatewayException, IOException {
        doThrow(new IOException()).when(hBaseClient).removeNamespace(ORG_NAMESPACE);

        hBaseGateway.removeOrganization(ORG);
    }

}
