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
package org.trustedanalytics.auth.gateway;

import com.google.common.base.Throwables;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.util.Base64;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SystemEnvironment {

    private static final String CONF_PROPERTY_XPATH = "/configuration/property";

    public static final String KRB_KDC = "KRB_KDC";
    public static final String KRB_REALM = "KRB_REALM";
    public static final String KRB_USER = "KRB_USER";
    public static final String KRB_PASSWORD = "KRB_PASSWORD";

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemEnvironment.class);
    private static final String HADOOP_PROVIDED_ZIP = "HADOOP_PROVIDED_ZIP";

    public String getVariable(String varName) {
        return Optional.ofNullable(System.getenv(varName)).orElseGet(() -> {
            String errorMsg = getErrorMsg(varName);
            LOGGER.error(errorMsg);
            throw new NullPointerException(errorMsg);
        });
    }

    private static String getErrorMsg(String varName) {
        return varName + " not found in ENVIRONMENT";
    }

    public Configuration getHadoopConfiguration() throws IOException {
        String encodedZip = getVariable(HADOOP_PROVIDED_ZIP);
        Map<String, String> configParams = getAsMap(encodedZip);

        Configuration configuration = new Configuration();
        configParams.entrySet().forEach(pair -> configuration.set(pair.getKey(), pair.getValue()));
        return configuration;
    }

    private Map<String, String> getAsMap(String encodedZip) throws IOException {
        InputStream zipInputStream =
          new ZipInputStream(new ByteArrayInputStream(Base64.decodeBase64(encodedZip)));
        ZipEntry zipFileEntry;
        Map<String, String> map = new HashMap<>();
        while ((zipFileEntry = ((ZipInputStream) zipInputStream).getNextEntry()) != null) {
            if (!zipFileEntry.getName().endsWith("-site.xml")) {
                continue;
            }
            byte[] bytes = IOUtils.toByteArray(zipInputStream);
            InputSource is = new InputSource(new ByteArrayInputStream(bytes));
            XPath xPath = XPathFactory.newInstance().newXPath();
            try {
                NodeList nodeList =
                  (NodeList) xPath.evaluate(CONF_PROPERTY_XPATH, is, XPathConstants.NODESET);

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node propNode = nodeList.item(i);
                    String key = (String) xPath.evaluate("name/text()", propNode, XPathConstants.STRING);
                    String value = (String) xPath.evaluate("value/text()", propNode, XPathConstants.STRING);
                    map.put(key, value);
                }
            } catch (XPathExpressionException e) {
                String errorMsg = "Reading hadoop configuration failed";
                LOGGER.error(errorMsg, e);
                Throwables.propagateIfPossible(e, IOException.class);
                throw new IOException(errorMsg, e);
            }
        }
        return map;
    }
}
