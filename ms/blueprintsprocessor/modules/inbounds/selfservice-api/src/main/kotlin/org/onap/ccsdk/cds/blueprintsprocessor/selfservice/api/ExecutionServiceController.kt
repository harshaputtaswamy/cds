/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM, Bell Canada.
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

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ACTION_MODE_ASYNC
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.cluster.optionalClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.mdcWebCoroutineScope
import org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.utils.determineHttpStatusCode
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.httpProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogCodes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.Phaser
import javax.annotation.PreDestroy

@RestController
@RequestMapping("/api/v1/execution-service")
@Api(
    value = "Execution Service Catalog",
    description = "Interaction with CBA which are available in CDS"
)
open class ExecutionServiceController {

    val log = logger(ExecutionServiceController::class)

    private val ph = Phaser(1)

    @Autowired
    lateinit var executionServiceHandler: ExecutionServiceHandler

    @Autowired
    lateinit var primaryEntityManager: LocalContainerEntityManagerFactoryBean

    @RequestMapping(
        path = ["/health-check"],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    @ApiOperation(value = "Health Check", hidden = true)
    suspend fun executionServiceControllerHealthCheck(
        @RequestParam(required = false, defaultValue = "false") checkDependencies: Boolean
    ): ResponseEntity<JsonNode> = mdcWebCoroutineScope {
        var body = mutableMapOf("success" to true)
        var statusCode = 200
        if (
            (
                BluePrintConstants.CLUSTER_ENABLED &&
                    BluePrintDependencyService.optionalClusterService()?.clusterJoined() != true
                ) ||
            (
                checkDependencies && !primaryEntityManager.dataSource.connection.isValid(1)
                )
        ) {
            statusCode = 503
            body.remove("success")
        }
        ResponseEntity.status(statusCode).body(body.asJsonType())
    }

    @RequestMapping(path = ["/process"], method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(
        value = "Execute a CBA workflow (action)",
        notes = "Execute the appropriate CBA's action based on the ExecutionServiceInput object passed as input.",
        produces = MediaType.APPLICATION_JSON_VALUE,
        response = ExecutionServiceOutput::class
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    suspend fun process(
        @ApiParam(value = "ExecutionServiceInput payload.", required = true)
        @RequestBody executionServiceInput: ExecutionServiceInput
    ): ResponseEntity<ExecutionServiceOutput> = mdcWebCoroutineScope(executionServiceInput) {
        if (executionServiceInput.actionIdentifiers.mode == ACTION_MODE_ASYNC) {
            throw httpProcessorException(
                ErrorCatalogCodes.GENERIC_FAILURE,
                SelfServiceApiDomains.BLUEPRINT_PROCESSOR,
                "Can't process async request through the REST endpoint. Use gRPC for async processing."
            )
        }
        ph.register()
        val processResult = executionServiceHandler.doProcess(executionServiceInput)
        ph.arriveAndDeregister()
        ResponseEntity(processResult, determineHttpStatusCode(processResult.status.code))
    }

    @PreDestroy
    fun preDestroy() {
        val name = "ExecutionServiceController"
        log.info("Starting to shutdown $name waiting for in-flight requests to finish ...")
        ph.arriveAndAwaitAdvance()
        log.info("Done waiting in $name")
    }
}
