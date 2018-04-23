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
package com.blackducksoftware.integration.hub.data.generator.push;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.data.generator.AbstractGenerator;
import com.blackducksoftware.integration.hub.data.generator.config.GeneratorConfig;
import com.blackducksoftware.integration.hub.data.generator.config.ProjectConfigHelper;
import com.blackducksoftware.integration.hub.service.CodeLocationService;

@Component
public class NotificationGenerator extends AbstractGenerator {
    private final static String HUB_PROJECT = "hub-alert-profiling-test";
    private static final String HUB_PROJECT_VERSION = "1.0.0";

    private final Logger logger = LoggerFactory.getLogger(NotificationGenerator.class);
    private final GeneratorConfig generatorConfig;
    private final ProjectConfigHelper projectConfigHelper;
    private int iteration = 0;

    @Autowired
    public NotificationGenerator(final GeneratorConfig generatorConfig, final ProjectConfigHelper projectConfigHelper) {
        super(generatorConfig);
        this.generatorConfig = generatorConfig;
        this.projectConfigHelper = projectConfigHelper;
        this.projectConfigHelper.setProjectName(HUB_PROJECT);
        this.projectConfigHelper.setProjectVersion(HUB_PROJECT_VERSION);
        this.projectConfigHelper.createProjectVersion();
    }

    @Scheduled(initialDelay = 30000, fixedRate = 60000)
    public void generateNotifications() {
        try {
            logger.info("Begin generating notifications");
            if (iteration % 2 == 0) {
                uploadBdio("bdio/notification/component-bdio.jsonld");
            } else {
                uploadBdio("bdio/notification/clean-bdio.jsonld");
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
        final CodeLocationService service = generatorConfig.getHubServicesFactory().createCodeLocationService();
        service.importBomFile(tempFile);
        tempFile.delete();
    }
}
