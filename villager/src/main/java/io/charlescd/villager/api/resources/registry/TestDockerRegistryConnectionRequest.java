/*
 * Copyright 2020 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.charlescd.villager.api.resources.registry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestDockerRegistryConnectionRequest {

    @JsonCreator
    public TestDockerRegistryConnectionRequest(@JsonProperty("artifactRepositoryConfigurationId") String artifactRepositoryConfigurationId) {
        this.artifactRepositoryConfigurationId = artifactRepositoryConfigurationId;
    }

    @NotEmpty
    protected String artifactRepositoryConfigurationId;

    public String getArtifactRepositoryConfigurationId() {
        return artifactRepositoryConfigurationId;
    }


}
