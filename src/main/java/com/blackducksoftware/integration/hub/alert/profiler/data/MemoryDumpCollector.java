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
package com.blackducksoftware.integration.hub.alert.profiler.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.request.Response;

@Component
public class MemoryDumpCollector extends SpringActuatorCollector {

    @Override
    @Scheduled(initialDelay = 300000, fixedRate = 1800000)
    public void collect() {
        collectDataAndCreateFile();
    }

    @Override
    public String getURL() {
        return "http://localhost:8080/heapdump";
    }

    @Override
    public void processResponse(final Response response) throws IOException, IntegrationException {
        try (final InputStream inputStream = response.getContent()) {
            final File file = createFile();
            FileUtils.copyInputStreamToFile(inputStream, file);
        }
    }

    @Override
    public File createFile() {
        final File parentDirectory = new File(getCollectorDataDirectory(), "heapdump");
        final String fileName = String.format("heap-%d.hprof.gz", System.currentTimeMillis());
        final File file = new File(parentDirectory, fileName);
        return file;
    }
}
