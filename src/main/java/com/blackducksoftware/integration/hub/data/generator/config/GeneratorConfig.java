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
package com.blackducksoftware.integration.hub.data.generator.config;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.data.generator.push.NotificationGenerator;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@Component
public class GeneratorConfig {
    private final Logger logger = LoggerFactory.getLogger(NotificationGenerator.class);

    private final static String HUB_PROJECT = "hub-alert-profiling-test";

    @Value("${blackduck.hub.url}")
    private String hubUrl;

    @Value("${blackduck.hub.api.key}")
    private String hubApiKey;

    @Value("${blackduck.hub.timeout}")
    private String hubTimeout;

    private HubServicesFactory hubServicesFactory;

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
        try {
            final Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
            final HubServerConfigBuilder builder = new HubServerConfigBuilder();
            builder.setAlwaysTrustServerCertificate(true);
            builder.setApiToken(hubApiKey);
            builder.setHubUrl(hubUrl);
            builder.setTimeout(hubTimeout);
            final HubServerConfig hubConfig = builder.build();
            final RestConnection restConnection = hubConfig.createRestConnection(intLogger);
            hubServicesFactory = new HubServicesFactory(restConnection);
        } catch (final EncryptionException ex) {
            logger.error("Error creating connection", ex);
        }
    }

    public String getHubUrl() {
        return hubUrl;
    }

    public void setHubUrl(final String hubUrl) {
        this.hubUrl = hubUrl;
    }

    public String getHubApiKey() {
        return hubApiKey;
    }

    public void setHubApiKey(final String hubApiKey) {
        this.hubApiKey = hubApiKey;
    }

    public String getHubTimeout() {
        return hubTimeout;
    }

    public void setHubTimeout(final String hubTimeout) {
        this.hubTimeout = hubTimeout;
    }

    public HubServicesFactory getHubServicesFactory() {
        return hubServicesFactory;
    }

    public void setHubServicesFactory(final HubServicesFactory hubServicesFactory) {
        this.hubServicesFactory = hubServicesFactory;
    }
}
