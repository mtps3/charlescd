/*
 *
 *  Copyright 2020 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.charlescd.moove.application

import io.charlescd.moove.domain.*
import io.charlescd.moove.domain.exceptions.NotFoundException
import io.charlescd.moove.domain.repository.BuildRepository
import io.charlescd.moove.domain.service.HermesService
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Named

@Named
class WebhookEventService(private val hermesService: HermesService, private val buildRepository: BuildRepository) {

    fun notifyDeploymentEvent(simpleWebhookEvent: SimpleWebhookEvent, deployment: Deployment) {
        hermesService.notifySubscriptionEvent(
            buildWebhookDeploymentEventType(simpleWebhookEvent, deployment))
    }

    private fun buildWebhookDeploymentEventType(simpleWebhookEvent: SimpleWebhookEvent, deployment: Deployment): WebhookDeploymentEventType {
        return WebhookDeploymentEventType(
            simpleWebhookEvent.workspaceId,
            simpleWebhookEvent.eventType,
            simpleWebhookEvent.eventStatus,
            buildWebhookDeploymentEvent(deployment, simpleWebhookEvent)
        )
    }

    private fun buildWebhookDeploymentEvent(deployment: Deployment, simpleWebhookEvent: SimpleWebhookEvent): WebhookDeploymentEvent {
        return WebhookDeploymentEvent(
            workspaceId = deployment.workspaceId,
            type = simpleWebhookEvent.eventType,
            status = simpleWebhookEvent.eventStatus,
            date = getDeploymentDateEvent(deployment, simpleWebhookEvent.eventType),
            timeExecution = getTimeExecutionEvent(deployment, simpleWebhookEvent),
            author = getAuthorEvent(deployment),
            circle = getCircleEvent(deployment),
            release = getReleaseEvent(deployment)
        )
    }

    private fun getTimeExecutionEvent(deployment: Deployment, simpleWebhookEvent: SimpleWebhookEvent): Long? {
        if (simpleWebhookEvent.eventType == WebhookEventTypeEnum.FINISH_DEPLOY &&
            simpleWebhookEvent.eventStatus == WebhookEventStatusEnum.SUCCESS) {
            return ChronoUnit.SECONDS.between(deployment.deployedAt, deployment.createdAt)
        }
        return null
    }

    private fun getAuthorEvent(deployment: Deployment): WebhookDeploymentAuthorEvent {
        return WebhookDeploymentAuthorEvent(
            deployment.author.email,
            deployment.author.name
        )
    }

    private fun getCircleEvent(deployment: Deployment): WebhookDeploymentCircleEvent {
        return WebhookDeploymentCircleEvent(
            deployment.circle.id,
            deployment.circle.name
        )
    }

    private fun getReleaseEvent(deployment: Deployment): WebhookDeploymentReleaseEvent {
        val build = getBuild(deployment.buildId)
        return WebhookDeploymentReleaseEvent(
            tag = build.tag,
            features = build.features.map { getFeatureEvent(it) },
            modules = emptyList() // TODO VERIFICAR COMO PEGAR ESSA INFORMACAO
        )
    }

    private fun getBuild(id: String): Build {
        return this.buildRepository.findById(
            id
        ).orElseThrow {
            NotFoundException("build", id)
        }
    }

    private fun getFeatureEvent(feature: FeatureSnapshot): WebhookDeploymentFeatureEvent {
        return WebhookDeploymentFeatureEvent(
            name = feature.name,
            branchName = feature.branchName
        )
    }

    private fun getDeploymentDateEvent(deployment: Deployment, webhookEventType: WebhookEventTypeEnum): LocalDateTime? {
        return when (webhookEventType) {
            WebhookEventTypeEnum.START_DEPLOY -> deployment.createdAt
            WebhookEventTypeEnum.START_UNDEPLOY -> LocalDateTime.now()
            WebhookEventTypeEnum.FINISH_DEPLOY -> getFinishDeployDate(deployment)
            WebhookEventTypeEnum.FINISH_UNDEPLOY -> getFinishUndeployDate(deployment)
        }
    }

    private fun getFinishDeployDate(deployment: Deployment): LocalDateTime? {
        if (deployment.deployedAt != null) {
            return deployment.deployedAt
        }
        return deployment.createdAt
    }

    private fun getFinishUndeployDate(deployment: Deployment): LocalDateTime? {
        if (deployment.undeployedAt != null) {
            return deployment.undeployedAt
        }
        return deployment.createdAt
    }
}