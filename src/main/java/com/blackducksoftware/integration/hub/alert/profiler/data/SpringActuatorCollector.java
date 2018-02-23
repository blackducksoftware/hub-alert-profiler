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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.rest.HttpMethod;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.rest.UnauthenticatedRestConnectionBuilder;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

public abstract class SpringActuatorCollector extends DataCollector {

    @Value("${blackduck.alert.profiler.data.directory}")
    private String collectorDataDirectory;

    public Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    public void collectDataAndCreateFile() {
        try {
            final RestConnection restConnection = createRestConnection();
            Request.Builder requestBuilder = new Request.Builder();
            requestBuilder = requestBuilder.method(HttpMethod.GET).uri(getURL()).mimeType("application/octet-stream");
            final Request request = requestBuilder.build();
            final HubServicesFactory factory = new HubServicesFactory(restConnection);
            final HubService service = factory.createHubService();
            final Response response = service.executeRequest(request);
            processResponse(response);
        } catch (final Exception ex) {
            getLogger().error("Error occurred collecting data...", ex);
        }
    }

    public RestConnection createRestConnection() {
        final Slf4jIntLogger intLogger = new Slf4jIntLogger(getLogger());
        final UnauthenticatedRestConnectionBuilder builder = new UnauthenticatedRestConnectionBuilder();
        builder.setBaseUrl("http://localhost:8080");
        builder.setLogger(intLogger);
        return builder.build();
    }

    public String getCollectorDataDirectory() {
        return collectorDataDirectory;
    }

    public abstract String getURL();

    public abstract void processResponse(final Response response) throws IOException, IntegrationException;

    public abstract File createFile();
}
