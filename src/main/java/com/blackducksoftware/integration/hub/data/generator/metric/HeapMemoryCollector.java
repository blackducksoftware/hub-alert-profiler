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
package com.blackducksoftware.integration.hub.data.generator.metric;

import java.io.IOException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.data.generator.JMXClient;
import com.blackducksoftware.integration.hub.data.generator.data.DataCollector;
import com.blackducksoftware.integration.hub.data.generator.mbean.HeapMemoryDescriptor;

@Component
public class HeapMemoryCollector extends DataCollector {

    final Logger logger = LoggerFactory.getLogger(HeapMemoryCollector.class);

    public final static String MEM_KEY_INIT = "init";
    public final static String MEM_KEY_COMMITTED = "committed";
    public final static String MEM_KEY_MAX = "max";
    public final static String MEM_KEY_USED = "used";

    private final JMXClient jmxClient;

    private CompositeData previousData;

    @Autowired
    public HeapMemoryCollector(final JMXClient jmxClient) {
        this.jmxClient = jmxClient;
    }

    @Override
    @Scheduled(initialDelay = 60000, fixedRate = 30000)
    public void collect() {
        try {
            final Object result = jmxClient.getMBeanAttribute(new HeapMemoryDescriptor());
            final CompositeData compositeData = (CompositeData) result;
            printDifferences(previousData, compositeData);
        } catch (AttributeNotFoundException | InstanceNotFoundException | MalformedObjectNameException | MBeanException | ReflectionException | IOException ex) {
            logger.error("Issue collecting metrics {}", ex);
        }
    }

    public void printDifferences(final CompositeData oldData, final CompositeData newData) {
        Long previousInitMem = 0L;
        Long previousMaxMem = 0L;
        Long previousUsedMem = 0L;
        Long previousCommittedMem = 0L;
        if (oldData != null) {
            previousInitMem = (Long) oldData.get(MEM_KEY_INIT);
            previousMaxMem = (Long) oldData.get(MEM_KEY_MAX);
            previousUsedMem = (Long) oldData.get(MEM_KEY_USED);
            previousCommittedMem = (Long) oldData.get(MEM_KEY_COMMITTED);
        }
        final Long initMem = (Long) newData.get(MEM_KEY_INIT);
        final Long maxMem = (Long) newData.get(MEM_KEY_MAX);
        final Long usedMem = (Long) newData.get(MEM_KEY_USED);
        final Long committedMem = (Long) newData.get(MEM_KEY_COMMITTED);

        logger.info("-------------------------------");
        logger.info("Heap Memory Collector ");
        logger.info("Init Memory:      {} MB", calculateDifference(initMem, previousInitMem));
        logger.info("Max Memory:       {} MB", calculateDifference(maxMem, previousMaxMem));
        logger.info("Committed Memory: {} MB", calculateDifference(committedMem, previousCommittedMem));
        logger.info("Used Memory:      {} MB", calculateDifference(usedMem, previousUsedMem));
        logger.info("-------------------------------");
    }

    private long calculateDifference(final Long oldValue, final Long newValue) {
        final long difference = Math.subtractExact(newValue, oldValue);
        final long megabytes = (difference / 1024) / 1024;
        return Math.abs(megabytes);
    }
}
