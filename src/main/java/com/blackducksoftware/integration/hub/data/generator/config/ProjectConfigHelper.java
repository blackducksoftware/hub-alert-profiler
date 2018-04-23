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

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.service.ProjectService;
import com.blackducksoftware.integration.hub.service.model.ProjectRequestBuilder;

@Component
public class ProjectConfigHelper {

    private final Logger logger = LoggerFactory.getLogger(ProjectConfigHelper.class);

    private final ProjectService projectService;
    private String projectName;
    private String projectVersion;

    @Autowired
    public ProjectConfigHelper(final GeneratorConfig generatorConfig) {
        this.projectService = generatorConfig.getHubServicesFactory().createProjectService();
    }

    public void createProjectVersion() {
        try {
            final ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder();
            projectRequestBuilder.setProjectName(getProjectName());
            projectRequestBuilder.setVersionName(getProjectVersion());
            projectService.createHubProject(projectRequestBuilder.build());
        } catch (final IntegrationException ex) {
            logger.info("Error creating project {} {}", getProjectName(), getProjectVersion());
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            final ProjectView createdProject = getProjectDataService().getProjectByName(getProjectName());
            getProjectDataService().deleteHubProject(createdProject);
        } catch (final IntegrationException ex) {
            logger.info("Error Deleting project {}", getProjectName(), ex);
        }
    }

    private ProjectService getProjectDataService() {
        return projectService;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public void setProjectVersion(final String projectVersion) {
        this.projectVersion = projectVersion;
    }
}
