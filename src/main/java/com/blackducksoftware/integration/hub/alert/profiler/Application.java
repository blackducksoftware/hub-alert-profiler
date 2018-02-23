/**
 * hub-alert-profiler
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
package com.blackducksoftware.integration.hub.alert.profiler;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.blackducksoftware.integration.hub.alert.profiler.data.DataCollector;

@EnableScheduling
@SpringBootApplication
public class Application {

    private final Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    private JMXClient jmxClient;

    @Autowired
    private List<DataCollector> collectorList;

    public static void main(final String[] args) {
        new SpringApplicationBuilder(Application.class).logStartupInfo(false).run(args);
    }

    @PostConstruct
    public void initJMXConnection() {
        try {
            jmxClient.connect();
        } catch (final IOException ex) {
            logger.error("Error connecting to JMX client", ex);
        }
    }

    @PreDestroy
    public void cleanUp() {
        try {
            if (!collectorList.isEmpty()) {
                collectorList.forEach(collector -> {
                    collector.collect();
                });
            }
            jmxClient.disconnect();
        } catch (final IOException ex) {
            logger.error("Error disconnecting to JMX client", ex);
        }
    }
}
