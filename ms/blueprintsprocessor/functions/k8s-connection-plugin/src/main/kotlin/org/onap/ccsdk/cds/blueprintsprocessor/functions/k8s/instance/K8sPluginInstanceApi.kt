/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
 * Modifications Copyright © 2022 Orange.
 * Modifications Copyright © 2020 Deutsche Telekom AG.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.K8sConnectionPluginConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance.K8sRbInstanceRestClient.Companion.getK8sRbInstanceRestClient
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance.healthcheck.K8sRbInstanceHealthCheck
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance.healthcheck.K8sRbInstanceHealthCheckList
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance.healthcheck.K8sRbInstanceHealthCheckSimple
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT

class K8sPluginInstanceApi(
    private val k8sConfiguration: K8sConnectionPluginConfiguration
) {
    private val log = LoggerFactory.getLogger(K8sPluginInstanceApi::class.java)!!

    fun getInstanceList(): List<K8sRbInstance>? {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val objectMapper = jacksonObjectMapper()
                val parsedObject: ArrayList<K8sRbInstance>? = objectMapper.readValue(result.body)
                parsedObject
            } else if (result.status == 500 && result.body.contains("Did not find any objects with tag"))
                null
            else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun getInstanceById(instanceId: String): K8sRbInstance? {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sRbInstance? = JacksonUtils.readValue(result.body, K8sRbInstance::class.java)
                parsedObject
            } else if (result.status == 500 && result.body.contains("Error finding master table"))
                null
            else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun getFullInstanceById(instanceId: String): K8sRbInstanceFull? {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "?full=true",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sRbInstanceFull? = JacksonUtils.readValue(result.body, K8sRbInstanceFull::class.java)
                parsedObject
            } else if (result.status == 500 && result.body.contains("Error finding master table"))
                null
            else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun getInstanceByRequestProperties(
        rbDefinitionName: String,
        rbDefinitionVersion: String,
        rbProfileName: String
    ): K8sRbInstance? {
        val instances: List<K8sRbInstance>? = this.getInstanceList()
        instances?.forEach {
            if (it.request?.rbName == rbDefinitionName && it.request?.rbVersion == rbDefinitionVersion &&
                it.request?.profileName == rbProfileName
            )
                return it
        }
        return null
    }

    fun getInstanceStatus(instanceId: String): K8sRbInstanceStatus? {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "/status",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sRbInstanceStatus? = JacksonUtils.readValue(
                    result.body, K8sRbInstanceStatus::class.java
                )
                parsedObject
            } else if (result.status == 500 && result.body.contains("Error finding master table"))
                null
            else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun queryInstanceStatus(
        instanceId: String,
        kind: String,
        apiVersion: String,
        name: String? = null,
        labels: Map<String, String>? = null
    ): K8sRbInstanceStatus? {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            var path: String = "/query?ApiVersion=$apiVersion&Kind=$kind"
            if (name != null)
                path = path.plus("&Name=$name")
            if (labels != null && labels.isNotEmpty()) {
                path = path.plus("&Labels=")
                for ((name, value) in labels)
                    path = path.plus("$name%3D$value,")
                path = path.trimEnd(',')
            }
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                path,
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sRbInstanceStatus? = JacksonUtils.readValue(
                    result.body, K8sRbInstanceStatus::class.java
                )
                parsedObject
            } else if (result.status == 500 && result.body.contains("Error finding master table"))
                null
            else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun getInstanceHealthCheckList(instanceId: String): K8sRbInstanceHealthCheckList? {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "/healthcheck",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sRbInstanceHealthCheckList? = JacksonUtils.readValue(
                    result.body,
                    K8sRbInstanceHealthCheckList::class.java
                )
                parsedObject
            } else if (result.status == 500 && result.body.contains("Error finding master table"))
                null
            else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun getInstanceHealthCheck(instanceId: String, healthCheckId: String): K8sRbInstanceHealthCheck? {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "/healthcheck/$healthCheckId",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sRbInstanceHealthCheck? = JacksonUtils.readValue(
                    result.body,
                    K8sRbInstanceHealthCheck::class.java
                )
                parsedObject
            } else if (result.status == 500 && result.body.contains("Error finding master table"))
                null
            else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun startInstanceHealthCheck(instanceId: String): K8sRbInstanceHealthCheckSimple? {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                POST.name,
                "/healthcheck",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sRbInstanceHealthCheckSimple? = JacksonUtils.readValue(
                    result.body,
                    K8sRbInstanceHealthCheckSimple::class.java
                )
                parsedObject
            } else if (result.status == 500 && result.body.contains("Error finding master table"))
                null
            else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun createConfigurationValues(configValues: K8sConfigValueRequest, instanceId: String): K8sConfigValueResponse? {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                POST.name,
                "/config",
                JacksonUtils.getJson(configValues)
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sConfigValueResponse? = JacksonUtils.readValue(
                    result.body, K8sConfigValueResponse::class.java
                )
                parsedObject
            } else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to create config instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun editConfigurationValues(configValues: K8sConfigValueRequest, instanceId: String, configName: String): K8sConfigValueResponse? {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                PUT.name,
                "/config/$configName",
                JacksonUtils.getJson(configValues)
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sConfigValueResponse? = JacksonUtils.readValue(
                    result.body, K8sConfigValueResponse::class.java
                )
                parsedObject
            } else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to edit config instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun editConfigurationValuesByDelete(instanceId: String, configName: String): K8sConfigValueResponse? {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                POST.name,
                "/config/$configName/delete",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sConfigValueResponse? = JacksonUtils.readValue(
                    result.body, K8sConfigValueResponse::class.java
                )
                parsedObject
            } else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to delete config instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun hasConfigurationValues(instanceId: String, configName: String): Boolean {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "/config/$configName",
                ""
            )
            log.debug(result.toString())
            return result.status in 200..299
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun hasConfigurationValuesVersion(instanceId: String, configName: String, version: String): Boolean {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "/config/$configName/version/$version",
                ""
            )
            log.debug(result.toString())
            return result.status in 200..299
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun getConfigurationValues(instanceId: String, configName: String): K8sConfigValueResponse? {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "/config/$configName",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sConfigValueResponse? = JacksonUtils.readValue(
                    result.body, K8sConfigValueResponse::class.java
                )
                parsedObject
            } else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get config instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun getConfigurationValuesVersion(instanceId: String, configName: String, version: String): K8sConfigValueResponse? {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "/config/$configName/version/$version",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sConfigValueResponse? = JacksonUtils.readValue(
                    result.body, K8sConfigValueResponse::class.java
                )
                parsedObject
            } else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get config instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun getConfigurationValuesVersionByTag(instanceId: String, configName: String, tag: String): K8sConfigValueResponse? {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "/config/$configName/tag/$tag",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sConfigValueResponse? = JacksonUtils.readValue(
                    result.body, K8sConfigValueResponse::class.java
                )
                parsedObject
            } else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get config instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun getConfigurationValuesList(instanceId: String): List<K8sConfigValueResponse>? {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "/config",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val objectMapper = jacksonObjectMapper()
                val parsedObject: ArrayList<K8sConfigValueResponse>? = objectMapper.readValue(result.body)
                parsedObject
            } else if (result.status == 500 && result.body.contains("Did not find any objects with tag"))
                null
            else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s config instance list")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun getConfigurationValuesVersionList(instanceId: String, configName: String): List<K8sConfigValueResponse>? {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "/config/$configName/version",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val objectMapper = jacksonObjectMapper()
                val parsedObject: ArrayList<K8sConfigValueResponse>? = objectMapper.readValue(result.body)
                parsedObject
            } else if (result.status == 500 && result.body.contains("Did not find any objects with tag"))
                null
            else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s config instance version list")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun getConfigurationValuesTagList(instanceId: String, configName: String): List<K8sConfigValueTag>? {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "/config/$configName/tag",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val objectMapper = jacksonObjectMapper()
                val parsedObject: ArrayList<K8sConfigValueTag>? = objectMapper.readValue(result.body)
                parsedObject
            } else if (result.status == 500 && result.body.contains("Did not find any objects with tag"))
                null
            else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s config instance tag list")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun deleteConfigurationValues(instanceId: String, configName: String, deleteConfigOnly: Boolean) {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            var path: String = "/config/$configName"
            if (deleteConfigOnly)
                path = path.plus("?deleteConfigOnly=true")
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                DELETE.name,
                path,
                ""
            )
            log.debug(result.toString())
            if (result.status !in 200..299)
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to delete config instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun rollbackConfigurationValues(instanceId: String, configName: String, configVersion: String?, configTag: String?) {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val configValues = hashMapOf<String, String>()
            if (configVersion != null)
                configValues["config-version"] = configVersion
            if (configTag != null)
                configValues["config-tag"] = configTag
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                POST.name,
                "/config/$configName/rollback",
                JacksonUtils.getJson(configValues)
            )
            log.debug(result.toString())
            if (result.status !in 200..299)
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to rollback config instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun tagConfigurationValues(instanceId: String, configName: String, tagName: String) {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val configValues = hashMapOf<String, String>()
            configValues["tag-name"] = tagName
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                POST.name,
                "/config/$configName/tagit",
                JacksonUtils.getJson(configValues)
            )
            log.debug(result.toString())
            if (result.status !in 200..299)
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to tag config instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun deleteInstanceHealthCheck(instanceId: String, healthCheckId: String) {
        val rbInstanceService = getK8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                DELETE.name,
                "/healthcheck/$healthCheckId",
                ""
            )
            log.debug(result.toString())
            if (result.status !in 200..299)
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }
}
