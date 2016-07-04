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
package org.trustedanalytics.auth.gateway.hdfs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.AclEntry;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.auth.gateway.hdfs.config.ExternalConfiguration;
import org.trustedanalytics.auth.gateway.hdfs.config.FileSystemProvider;
import org.trustedanalytics.auth.gateway.hdfs.kerberos.KerberosProperties;
import org.trustedanalytics.auth.gateway.hdfs.utils.PathCreator;
import org.trustedanalytics.auth.gateway.hdfs.utils.Qualifiers;
import org.trustedanalytics.auth.gateway.spi.Authorizable;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

import java.io.IOException;
import java.util.List;

@Profile(Qualifiers.HDFS)
@Configuration
public class HdfsGateway implements Authorizable {

  private static final String NAME = "hdfs";

  private static final String ADMIN_POSTFIX = "_admin";

  private static final String SYS_GROUP_POSTFIX = "_sys";

  @Autowired
  private KerberosProperties krbProperties;

  @Autowired
  private ExternalConfiguration config;

  @Autowired
  private PathCreator paths;

  @Autowired
  private FileSystemProvider fileSystemProvider;

  @Override
  public void addOrganization(String orgId) throws AuthorizableGatewayException {
    FsPermission usrAllGroupAll = HdfsPermission.USER_ALL_GROUP_ALL.getPermission();
    FsPermission usrAllGroupRead = HdfsPermission.USER_ALL_GROUP_READ.getPermission();
    List<AclEntry> defaultWithKrbTechUserExec = getDefaultAclWithKrbTechUserAction(FsAction.ALL,
        FsAction.EXECUTE, orgId.concat(SYS_GROUP_POSTFIX));
    List<AclEntry> defaultWithDefaultKrbTechUserExec = getDefaultAclWithDefaultKrbTechUserAction(FsAction.ALL,
            FsAction.EXECUTE, orgId.concat(SYS_GROUP_POSTFIX));
    List<AclEntry> defaultWithTechUserAll = getDefaultAclWithKrbTechUserAction(FsAction.ALL,
        FsAction.ALL, orgId.concat(SYS_GROUP_POSTFIX));
    String orgAdmin = orgId.concat(ADMIN_POSTFIX);

    try {
      HdfsClient hdfsClient = HdfsClient.getNewInstance(fileSystemProvider.getFileSystem());

      hdfsClient.createDirectoryWithAcl(paths.getOrgPath(orgId), orgAdmin, orgId, usrAllGroupRead,
          defaultWithKrbTechUserExec);

      hdfsClient
          .createDirectoryWithAcl(paths.getBrokerPath(orgId), orgAdmin, orgId, usrAllGroupAll,
              defaultWithDefaultKrbTechUserExec);

      hdfsClient.updateDirectoryWithSubdirectories(paths.getBrokerPath(orgId), orgId, usrAllGroupAll);
      hdfsClient.setACLForDirectoryWithSubdirectories(paths.getBrokerPath(orgId), defaultWithDefaultKrbTechUserExec);

      hdfsClient
          .createDirectoryWithAcl(paths.getUserspacePath(orgId), orgAdmin, orgId, usrAllGroupRead,
              defaultWithTechUserAll);

      hdfsClient
          .createDirectoryWithAcl(paths.getSqoopImportsPath(orgId), orgAdmin, orgId, usrAllGroupAll,
              defaultWithTechUserAll);

      hdfsClient.updateDirectoryWithSubdirectories(paths.getSqoopImportsPath(orgId), orgId, usrAllGroupRead);
      hdfsClient.setACLForDirectoryWithSubdirectories(paths.getSqoopImportsPath(orgId),
              defaultWithDefaultKrbTechUserExec);

      hdfsClient.createDirectory(paths.getOozieJobsPath(orgId), orgAdmin, orgId, usrAllGroupAll);
      hdfsClient.createDirectory(paths.getUsersPath(orgId), orgAdmin, orgId, usrAllGroupRead);
      hdfsClient.createDirectory(paths.getTmpPath(orgId), orgAdmin, orgId, usrAllGroupAll);
      hdfsClient.createDirectory(paths.getSharedPath(orgId), orgAdmin, orgId, usrAllGroupAll);

      hdfsClient.createDirectory(paths.getAppPath(orgId), orgAdmin, orgId, usrAllGroupRead);
      hdfsClient.updateDirectoryWithSubdirectories(paths.getAppPath(orgId), orgId, usrAllGroupRead);
    } catch (IOException e) {
      throw new AuthorizableGatewayException(String.format("Can't add organization: %s", orgId), e);
    }
  }

  @Override
  public void removeOrganization(String orgId) throws AuthorizableGatewayException {
    try {
      HdfsClient hdfsClient = HdfsClient.getNewInstance(fileSystemProvider.getFileSystem());
      hdfsClient.deleteDirectory(paths.getOrgPath(orgId));
    } catch (IOException e) {
      throw new AuthorizableGatewayException(String.format("Can't remove organization: %s", orgId),
          e);
    }
  }

  @Override
  public void addUserToOrg(String userId, String orgId) throws AuthorizableGatewayException {
    try {
      final HdfsClient hdfsClient = HdfsClient.getNewInstance(fileSystemProvider.getFileSystem());
      hdfsClient.createDirectory(paths.getUserPath(orgId, userId), userId, orgId,
          HdfsPermission.USER_ALL.getPermission());

      // user home directory is required by hadoop components (e.g. oozie) to store temporary files
      hdfsClient.createDirectory(paths.getUserHomePath(userId), userId, userId,
          HdfsPermission.USER_ALL.getPermission());
    } catch (IOException e) {
      throw new AuthorizableGatewayException(String.format("Can't add user: %s", userId), e);
    }
  }

  @Override
  public void removeUserFromOrg(String userId, String orgId) throws AuthorizableGatewayException {
    try {
      HdfsClient hdfsClient = HdfsClient.getNewInstance(fileSystemProvider.getFileSystem());
      hdfsClient.deleteDirectory(paths.getUserPath(orgId, userId));
    } catch (IOException e) {
      throw new AuthorizableGatewayException(
          String.format("Can't remove user: %s from org: %s", userId, orgId), e);
    }
  }

  @Override
  public void synchronize() throws AuthorizableGatewayException
  {
      try{
          final HdfsClient hdfsClient = HdfsClient.getNewInstance(fileSystemProvider.getFileSystem());

          //create home directory for defined users
          hdfsClient.createDirectory(paths.getUserHomePath("cf"), "cf", "cf",
                  HdfsPermission.USER_ALL.getPermission());
          hdfsClient.createDirectory(paths.getUserHomePath("vcap"), "vcap", "vcap",
                  HdfsPermission.USER_ALL.getPermission());
          hdfsClient.createDirectory(paths.getUserHomePath("h2o"), "h2o", "h2o",
                  HdfsPermission.USER_ALL.getPermission());
          hdfsClient.createDirectory(paths.getUserHomePath("root"), "root", "root",
                  HdfsPermission.USER_ALL.getPermission());

          hdfsClient.createDirectory(paths.getOrgsPath(), "authgateway", "supergroup",
                  HdfsPermission.USER_ALL_GROUP_EXECUTE.getPermission());

          hdfsClient.createDirectory(new Path("/h2o"), "h2o", "h2o",
                  HdfsPermission.USER_ALL.getPermission());
      } catch (IOException e) {
          throw new AuthorizableGatewayException(String.format("Can't sync hdfs"), e);
      }
  }

  @Override
  public String getName() {
    return NAME;
  }

  @VisibleForTesting
  List<AclEntry> getDefaultAclWithKrbTechUserAction(FsAction groupAction, FsAction techUserAction,
                                                    String sysOrg) {
    return HdfsAclBuilder.newInstanceWithDefaultEntries(groupAction)
            .withUsersAclEntry(ImmutableMap.of(
                    config.getArcadiaUser(), FsAction.EXECUTE,
                    config.getVcapUser(), FsAction.EXECUTE))
            .withUserAclEntry(krbProperties.getTechnicalPrincipal(), techUserAction)
            .withGroupAclEntry(sysOrg, FsAction.EXECUTE)
            .withGroupAclEntry(config.getHiveUser(), FsAction.READ_EXECUTE).build();
  }

  @VisibleForTesting
  List<AclEntry> getDefaultAclWithDefaultKrbTechUserAction(FsAction groupAction, FsAction techUserAction,
      String sysOrg) {
    return HdfsAclBuilder.newInstanceWithDefaultEntries(groupAction)
            .withUsersAclEntry(ImmutableMap.of(
                    config.getArcadiaUser(), FsAction.EXECUTE,
                    config.getVcapUser(), FsAction.EXECUTE))
            .withDefaultUsersAclEntry(ImmutableMap.of(
                    config.getArcadiaUser(), FsAction.EXECUTE,
                    config.getVcapUser(), FsAction.EXECUTE))
            .withUserAclEntry(krbProperties.getTechnicalPrincipal(), techUserAction)
            .withDefaultUserAclEntry(krbProperties.getTechnicalPrincipal(), techUserAction)
            .withGroupAclEntry(sysOrg, FsAction.EXECUTE)
            .withDefaultGroupAclEntry(sysOrg, FsAction.EXECUTE)
            .withGroupAclEntry(config.getHiveUser(), FsAction.READ_EXECUTE)
            .withDefaultGroupAclEntry(config.getHiveUser(), FsAction.READ_EXECUTE).build();
  }
}
