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
package org.trustedanalytics.auth.gateway.hdfs.integration.config;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.auth.gateway.hdfs.kerberos.KerberosProperties;

@Configuration
@Profile("test")
public class LocalConfiguration {

  @Bean
  public KerberosProperties getKerberosProperties() throws IOException {
    return new KerberosProperties("kdc", "krealm", "test_cf", "super", "base64");
  }

  @Bean
  public org.apache.hadoop.conf.Configuration initializeHdfsCluster() throws IOException,
      InterruptedException, URISyntaxException {
    File baseDir = new File("./target/hdfs/" + "testName").getAbsoluteFile();
    FileUtil.fullyDelete(baseDir);
    org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration(false);
    conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, baseDir.getAbsolutePath());
    conf.setBoolean(DFSConfigKeys.DFS_PERMISSIONS_ENABLED_KEY, true);
    conf.setBoolean(DFSConfigKeys.DFS_NAMENODE_ACLS_ENABLED_KEY, true);
    MiniDFSCluster.Builder builder = new MiniDFSCluster.Builder(conf);
    MiniDFSCluster cluster = builder.build();

    UserGroupInformation.createUserForTesting("cf", new String[] {"cf"});
    UserGroupInformation.createUserForTesting("super", new String[] {"supergroup"});

    return cluster.getConfiguration(0);
  }
}
