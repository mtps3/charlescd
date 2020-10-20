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

package io.charlescd.villager.interactor.registry.impl;

import io.charlescd.villager.interactor.registry.ComponentTagDTO;
import io.charlescd.villager.interactor.registry.GetDockerRegistryTagInput;
import io.charlescd.villager.interactor.registry.GetDockerRegistryTagInteractor;
import io.charlescd.villager.service.RegistryService;
import org.apache.http.HttpStatus;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class GetDockerRegistryTagInteractorImpl implements GetDockerRegistryTagInteractor {

    private RegistryService registryService;

    @Inject
    public GetDockerRegistryTagInteractorImpl(
            RegistryService registryService) {
        this.registryService = registryService;
    }

    @Override
    public Optional<ComponentTagDTO> execute(GetDockerRegistryTagInput input) {

        var entity =
                registryService.getDockerRegistryConfigurationEntity(input.getWorkspaceId(), input.getArtifactRepositoryConfigurationId());


        var response =
                registryService.getDockerRegistryTag(entity, input.getArtifactName(), input.getName());

        if (response.isEmpty() || response.get().getStatus() != HttpStatus.SC_OK) {
            return Optional.empty();
        }

        return Optional.of(new ComponentTagDTO(
                input.getName(),
                entity.connectionData.host + "/" + input.getArtifactName() + ":" + input.getName()
        ));


    }
}
