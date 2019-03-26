/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ACTION_MODE_ASYNC
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ACTION_MODE_SYNC
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.Status
import org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.utils.saveCBAFile
import org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.utils.toProto
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintWorkflowExecutionService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintFileUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.slf4j.LoggerFactory
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.stream.Collectors

@Service
class ExecutionServiceHandler(private val bluePrintCoreConfiguration: BluePrintCoreConfiguration,
                              private val bluePrintCatalogService: BluePrintCatalogService,
                              private val bluePrintWorkflowExecutionService
                              : BluePrintWorkflowExecutionService<ExecutionServiceInput, ExecutionServiceOutput>) {

    private val log = LoggerFactory.getLogger(ExecutionServiceHandler::class.toString())

    fun upload(filePart: FilePart): Mono<String> {
        try {
            val archivedPath = BluePrintFileUtils.getCbaStorageDirectory(bluePrintCoreConfiguration.archivePath)
            val cbaPath = saveCBAFile(filePart, archivedPath)
            bluePrintCatalogService.saveToDatabase(cbaPath.toFile()).let {
                return Mono.just("{\"status\": \"Successfully uploaded blueprint with id($it)\"}")
            }
        } catch (e: Exception) {
            return Mono.error<String>(BluePrintException("Error uploading the CBA file.", e))
        }
    }

    suspend fun process(executionServiceInput: ExecutionServiceInput,
                        responseObserver: StreamObserver<org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput>) {
        when {
            executionServiceInput.actionIdentifiers.mode == ACTION_MODE_ASYNC -> {
                GlobalScope.launch(Dispatchers.Default) {
                    val executionServiceOutput = doProcess(executionServiceInput)
                    responseObserver.onNext(executionServiceOutput.toProto())
                    responseObserver.onCompleted()
                }
                responseObserver.onNext(response(executionServiceInput).toProto())
            }
            executionServiceInput.actionIdentifiers.mode == ACTION_MODE_SYNC -> {
                val executionServiceOutput = doProcess(executionServiceInput)
                responseObserver.onNext(executionServiceOutput.toProto())
                responseObserver.onCompleted()
            }
            else -> responseObserver.onNext(response(executionServiceInput,
                "Failed to process request, 'actionIdentifiers.mode' not specified. Valid value are: 'sync' or 'async'.",
                true).toProto());
        }
    }

    suspend fun doProcess(executionServiceInput: ExecutionServiceInput): ExecutionServiceOutput {
        val requestId = executionServiceInput.commonHeader.requestId
        log.info("processing request id $requestId")

        val actionIdentifiers = executionServiceInput.actionIdentifiers

        val blueprintName = actionIdentifiers.blueprintName
        val blueprintVersion = actionIdentifiers.blueprintVersion

        val basePath = bluePrintCatalogService.getFromDatabase(blueprintName, blueprintVersion)
        log.info("blueprint base path $basePath")

        val blueprintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(requestId, basePath.toString())

        val output = bluePrintWorkflowExecutionService.executeBluePrintWorkflow(blueprintRuntimeService,
            executionServiceInput, hashMapOf())

        val errors = blueprintRuntimeService.getBluePrintError().errors
        if (errors.isNotEmpty()) {
            val errorMessage = errors.stream().map { it.toString() }.collect(Collectors.joining(", "))
            setErrorStatus(errorMessage, output.status)
        }

        return output
    }

    private fun setErrorStatus(errorMessage: String, status: Status) {
        status.errorMessage = errorMessage
        status.eventType = EventType.EVENT_COMPONENT_FAILURE.name
        status.code = 500
        status.message = BluePrintConstants.STATUS_FAILURE
    }

    private fun response(executionServiceInput: ExecutionServiceInput, errorMessage: String = "",
                         failure: Boolean = false): ExecutionServiceOutput {
        val executionServiceOutput = ExecutionServiceOutput()
        executionServiceOutput.commonHeader = executionServiceInput.commonHeader
        executionServiceOutput.actionIdentifiers = executionServiceInput.actionIdentifiers
        executionServiceOutput.payload = JsonNodeFactory.instance.objectNode()

        val status = Status()
        if (failure) {
            setErrorStatus(errorMessage, status)
        } else {
            status.eventType = EventType.EVENT_COMPONENT_PROCESSING.name
            status.code = 200
            status.message = BluePrintConstants.STATUS_PROCESSING
        }

        executionServiceOutput.status = status

        return executionServiceOutput
    }

}