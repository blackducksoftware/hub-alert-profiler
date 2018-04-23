/**
 * hub-data-generator
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.data.generator;

import java.io.IOException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.data.generator.mbean.MBeanAttributeDescriptor;

@Component
public class JMXClient {

    private JMXConnector connector;
    private MBeanServerConnection serverConnection;

    public void connect() throws IOException {
        if (connector != null) {
            connector.close();
        }
        final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:9045/jmxrmi");
        connector = JMXConnectorFactory.connect(url, null);
        serverConnection = connector.getMBeanServerConnection();
    }

    public void disconnect() throws IOException {
        if (connector != null) {
            connector.close();
        }
    }

    @SuppressWarnings(value = "unchecked")
    public <T> T getMBeanAttribute(final MBeanAttributeDescriptor descriptor) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        return (T) serverConnection.getAttribute(descriptor.getMbeanName(), descriptor.getAttributeName());
    }
}
