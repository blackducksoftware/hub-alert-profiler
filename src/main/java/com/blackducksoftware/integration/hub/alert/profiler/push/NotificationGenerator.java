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
package com.blackducksoftware.integration.hub.alert.profiler.push;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.CodeLocationService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.ProjectService;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@Component
public class NotificationGenerator {
    private final Logger logger = LoggerFactory.getLogger(NotificationGenerator.class);

    private final static String HUB_PROJECT = "hub-alert-profiling-test";

    @Value("${blackduck.hub.url}")
    private String hubUrl;

    @Value("${blackduck.hub.api.key}")
    private String hubApiKey;

    @Value("${blackduck.hub.timeout}")
    private String hubTimeout;

    private HubServicesFactory hubServicesFactory;
    private ProjectService projectDataService;
    private int iteration = 0;

    @PostConstruct
    public void init() throws IntegrationException {
        logger.info("===============================");
        logger.info("= Hub URL:     {}", hubUrl);
        if (StringUtils.isNotBlank(hubApiKey)) {
            logger.info("= Hub API Key: **********");
        } else {
            logger.info("= Hub API Key: (empty)");
        }
        logger.info("= Hub Timeout: {}", hubTimeout);
        logger.info("===============================");
        setupConnection();
    }

    private void setupConnection() {
        final Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setAlwaysTrustServerCertificate(true);
        builder.setApiKey(hubApiKey);
        builder.setHubUrl(hubUrl);
        builder.setTimeout(hubTimeout);
        final HubServerConfig hubConfig = builder.build();
        final RestConnection restConnection = hubConfig.createApiKeyRestConnection(intLogger);
        hubServicesFactory = new HubServicesFactory(restConnection);
        projectDataService = hubServicesFactory.createProjectService();
    }

    @PreDestroy
    public void cleanup() {
        try {
            final ProjectView createdProject = projectDataService.getProjectByName(HUB_PROJECT);
            projectDataService.deleteHubProject(createdProject);
        } catch (final IntegrationException ex) {
            logger.info("Error Deleting project {}", HUB_PROJECT, ex);
        }
    }

    @Scheduled(initialDelay = 30000, fixedRate = 60000)
    public void generateNotifications() {
        try {
            logger.info("Begin generating notifications");
            if (iteration % 2 == 0) {
                uploadBdio("bdio/component-bdio.jsonld");
            } else {
                uploadBdio("bdio/clean-bdio.jsonld");
            }
        } catch (final IntegrationException | URISyntaxException | IOException ex) {
            logger.info("Error creating notifications", ex);
        } finally {
            iteration++;
            logger.info("End generating notifications");
        }
    }

    private void uploadBdio(final String bdioFile) throws IntegrationException, URISyntaxException, IOException {
        final ClassPathResource classPathResource = new ClassPathResource(bdioFile);
        final File tempFile = File.createTempFile("tempBdio", ".jsonld");
        FileUtils.copyInputStreamToFile(classPathResource.getInputStream(), tempFile);
        logger.info("Bdio file to upload: {}", tempFile);
        final CodeLocationService service = hubServicesFactory.createCodeLocationService();
        service.importBomFile(tempFile);
        tempFile.delete();
    }
}
