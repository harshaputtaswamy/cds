/*
 * Copyright © 2017-2019 AT&T, Bell Canada, Nordix Foundation
 * Modifications Copyright © 2018-2019 IBM.
 * Modifications Copyright © 2019 Huawei.
 * Modifications Copyright © 2022 Deutsche Telekom AG.
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

package org.onap.ccsdk.cds.blueprintsprocessor.rest.service

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import org.apache.http.HttpEntity
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.EntityBuilder
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPatch
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLContextBuilder
import org.apache.http.conn.ssl.TrustAllStrategy
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService.WebClientResponse
import org.onap.ccsdk.cds.blueprintsprocessor.rest.utils.WebClientUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

abstract class BaseBlueprintWebClientService<out E : RestClientProperties> : BlueprintWebClientService {

    open fun host(uri: String): String {
        val uri: URI = URI.create(getRestClientProperties().url + uri)
        return uri.resolve(uri).toString()
    }

    abstract fun getRestClientProperties(): E

    open fun getRequestConfig(): RequestConfig {
        val requestConfigBuilder = RequestConfig.custom()
        if (getRestClientProperties().connectionRequestTimeout > 0)
            requestConfigBuilder.setConnectionRequestTimeout(getRestClientProperties().connectionRequestTimeout)
        if (getRestClientProperties().connectTimeout > 0)
            requestConfigBuilder.setConnectTimeout(getRestClientProperties().connectTimeout)
        if (getRestClientProperties().socketTimeout > 0)
            requestConfigBuilder.setSocketTimeout(getRestClientProperties().socketTimeout)
        return requestConfigBuilder.build()
    }

    open fun https_proxy(): String? {
        return getRestClientProperties().proxy
    }

    open fun httpClient(): CloseableHttpClient {
        var httpClients = HttpClients.custom()
        if (https_proxy() != null && https_proxy() != "") {
            val proxyProtocol = https_proxy()?.split(':')?.get(0) ?: "http"
            val proxyUri = https_proxy()?.split(':')?.get(1)?.replace("/", "") ?: ""
            val proxyPort = https_proxy()?.split(':')?.get(2)?.toInt() ?: 0
            if (proxyUri != "" && proxyPort != 0) {
                val proxy = HttpHost(proxyUri, proxyPort, proxyProtocol)
                httpClients = httpClients.setProxy(proxy)
                    .setSSLContext(SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            }
        }
        return httpClients
            .addInterceptorFirst(WebClientUtils.logRequest())
            .addInterceptorLast(WebClientUtils.logResponse())
            .setDefaultRequestConfig(getRequestConfig())
            .build()
    }

    override fun exchangeResource(methodType: String, path: String, request: String): WebClientResponse<String> {
        return this.exchangeResource(methodType, path, request, defaultHeaders())
    }

    override fun exchangeResource(
        methodType: String,
        path: String,
        request: String,
        headers: Map<String, String>
    ): WebClientResponse<String> {
        /**
         * TODO: Basic headers in the implementations of this client do not get added
         * in blocking version, whereas in NB version defaultHeaders get added.
         * the difference is in convertToBasicHeaders vs basicHeaders
         */
        val convertedHeaders: Array<BasicHeader> = convertToBasicHeaders(headers)
        return when (HttpMethod.resolve(methodType)) {
            HttpMethod.DELETE -> delete(path, convertedHeaders, String::class.java)
            HttpMethod.GET -> get(path, convertedHeaders, String::class.java)
            HttpMethod.POST -> post(path, request, convertedHeaders, String::class.java)
            HttpMethod.PUT -> put(path, request, convertedHeaders, String::class.java)
            HttpMethod.PATCH -> patch(path, request, convertedHeaders, String::class.java)
            else -> throw BluePrintProcessorException(
                "Unsupported methodType($methodType) attempted on path($path)"
            )
        }
    }

    @Throws(IOException::class, ClientProtocolException::class)
    protected fun performHttpCall(httpUriRequest: HttpUriRequest): WebClientResponse<String> {
        val httpResponse = httpClient().execute(httpUriRequest)
        val statusCode = httpResponse.statusLine.statusCode
        httpResponse.entity.content.use {
            val body = IOUtils.toString(it, Charset.defaultCharset())
            return WebClientResponse(statusCode, body)
        }
    }

    open override fun uploadBinaryFile(path: String, filePath: Path): WebClientResponse<String> {
        val convertedHeaders: Array<BasicHeader> = convertToBasicHeaders(defaultHeaders())
        val httpPost = HttpPost(host(path))
        val entity = EntityBuilder.create().setBinary(Files.readAllBytes(filePath)).build()
        httpPost.setEntity(entity)
        RestLoggerService.httpInvoking(convertedHeaders)
        httpPost.setHeaders(convertedHeaders)
        return performHttpCall(httpPost)
    }

    // TODO: convert to multi-map
    override fun convertToBasicHeaders(headers: Map<String, String>): Array<BasicHeader> {
        return headers.map { BasicHeader(it.key, it.value) }.toTypedArray()
    }

    open fun <T> delete(path: String, headers: Array<BasicHeader>, responseType: Class<T>): WebClientResponse<T> {
        val httpDelete = HttpDelete(host(path))
        RestLoggerService.httpInvoking(headers)
        httpDelete.setHeaders(headers)
        return performCallAndExtractTypedWebClientResponse(httpDelete, responseType)
    }

    open fun <T> get(path: String, headers: Array<BasicHeader>, responseType: Class<T>): WebClientResponse<T> {
        val httpGet = HttpGet(host(path))
        RestLoggerService.httpInvoking(headers)
        httpGet.setHeaders(headers)
        return performCallAndExtractTypedWebClientResponse(httpGet, responseType)
    }

    open fun <T> post(path: String, request: Any, headers: Array<BasicHeader>, responseType: Class<T>): WebClientResponse<T> {
        val httpPost = HttpPost(host(path))
        val entity = StringEntity(strRequest(request))
        httpPost.entity = entity
        RestLoggerService.httpInvoking(headers)
        httpPost.setHeaders(headers)
        return performCallAndExtractTypedWebClientResponse(httpPost, responseType)
    }

    open fun <T> put(path: String, request: Any, headers: Array<BasicHeader>, responseType: Class<T>): WebClientResponse<T> {
        val httpPut = HttpPut(host(path))
        val entity = StringEntity(strRequest(request))
        httpPut.entity = entity
        RestLoggerService.httpInvoking(headers)
        httpPut.setHeaders(headers)
        return performCallAndExtractTypedWebClientResponse(httpPut, responseType)
    }

    open fun <T> patch(path: String, request: Any, headers: Array<BasicHeader>, responseType: Class<T>): WebClientResponse<T> {
        val httpPatch = HttpPatch(host(path))
        val entity = StringEntity(strRequest(request))
        httpPatch.entity = entity
        RestLoggerService.httpInvoking(headers)
        httpPatch.setHeaders(headers)
        return performCallAndExtractTypedWebClientResponse(httpPatch, responseType)
    }

    /**
     * Perform the HTTP call and return HTTP status code and body.
     * @param httpUriRequest {@link HttpUriRequest} object
     * @return {@link WebClientResponse} object
     * http client may throw IOException and ClientProtocolException on error
     */

    @Throws(IOException::class, ClientProtocolException::class)
    protected fun <T> performCallAndExtractTypedWebClientResponse(
        httpUriRequest: HttpUriRequest,
        responseType: Class<T>
    ):
        WebClientResponse<T> {
            val httpResponse = httpClient().execute(httpUriRequest)
            val statusCode = httpResponse.statusLine.statusCode
            val entity: HttpEntity? = httpResponse.entity
            if (canResponseHaveBody(httpResponse)) {
                entity!!.content.use {
                    val body = getResponse(it, responseType)
                    return WebClientResponse(statusCode, body)
                }
            } else {
                val constructor = responseType.getConstructor()
                val body = constructor.newInstance()
                return WebClientResponse(statusCode, body)
            }
        }
    fun canResponseHaveBody(response: HttpResponse): Boolean {
        val status = response.statusLine.statusCode
        return response.entity !== null &&
            status != HttpStatus.SC_NO_CONTENT &&
            status != HttpStatus.SC_NOT_MODIFIED &&
            status != HttpStatus.SC_RESET_CONTENT
    }

    open suspend fun getNB(path: String): WebClientResponse<String> {
        return getNB(path, null, String::class.java)
    }

    open suspend fun getNB(path: String, additionalHeaders: Array<BasicHeader>?): WebClientResponse<String> {
        return getNB(path, additionalHeaders, String::class.java)
    }

    open suspend fun <T> getNB(path: String, additionalHeaders: Array<BasicHeader>?, responseType: Class<T>):
        WebClientResponse<T> = withContext(Dispatchers.IO) {
            get(path, additionalHeaders!!, responseType)
        }

    open suspend fun postNB(path: String, request: Any): WebClientResponse<String> {
        return postNB(path, request, null, String::class.java)
    }

    open suspend fun postNB(path: String, request: Any, additionalHeaders: Array<BasicHeader>?): WebClientResponse<String> {
        return postNB(path, request, additionalHeaders, String::class.java)
    }

    open suspend fun <T> postNB(
        path: String,
        request: Any,
        additionalHeaders: Array<BasicHeader>?,
        responseType: Class<T>
    ): WebClientResponse<T> = withContext(Dispatchers.IO) {
        post(path, request, additionalHeaders!!, responseType)
    }

    open suspend fun putNB(path: String, request: Any): WebClientResponse<String> {
        return putNB(path, request, null, String::class.java)
    }

    open suspend fun putNB(
        path: String,
        request: Any,
        additionalHeaders: Array<BasicHeader>?
    ): WebClientResponse<String> {
        return putNB(path, request, additionalHeaders, String::class.java)
    }

    open suspend fun <T> putNB(
        path: String,
        request: Any,
        additionalHeaders: Array<BasicHeader>?,
        responseType: Class<T>
    ): WebClientResponse<T> = withContext(Dispatchers.IO) {
        put(path, request, additionalHeaders!!, responseType)
    }

    open suspend fun <T> deleteNB(path: String): WebClientResponse<String> {
        return deleteNB(path, null, String::class.java)
    }

    open suspend fun <T> deleteNB(path: String, additionalHeaders: Array<BasicHeader>?):
        WebClientResponse<String> {
            return deleteNB(path, additionalHeaders, String::class.java)
        }

    open suspend fun <T> deleteNB(path: String, additionalHeaders: Array<BasicHeader>?, responseType: Class<T>):
        WebClientResponse<T> = withContext(Dispatchers.IO) {
            delete(path, additionalHeaders!!, responseType)
        }

    open suspend fun <T> patchNB(path: String, request: Any, additionalHeaders: Array<BasicHeader>?, responseType: Class<T>):
        WebClientResponse<T> = withContext(Dispatchers.IO) {
            patch(path, request, additionalHeaders!!, responseType)
        }

    override suspend fun exchangeNB(methodType: String, path: String, request: Any): WebClientResponse<String> {
        return exchangeNB(
            methodType, path, request, hashMapOf(),
            String::class.java
        )
    }

    override suspend fun exchangeNB(methodType: String, path: String, request: Any, additionalHeaders: Map<String, String>?):
        WebClientResponse<String> {
            return exchangeNB(methodType, path, request, additionalHeaders, String::class.java)
        }

    override suspend fun <T> exchangeNB(
        methodType: String,
        path: String,
        request: Any,
        additionalHeaders: Map<String, String>?,
        responseType: Class<T>
    ): WebClientResponse<T> {

        // TODO: possible inconsistency
        // NOTE: this basic headers function is different from non-blocking
        val convertedHeaders: Array<BasicHeader> = basicHeaders(additionalHeaders!!)
        return when (HttpMethod.resolve(methodType)) {
            HttpMethod.GET -> getNB(path, convertedHeaders, responseType)
            HttpMethod.POST -> postNB(path, request, convertedHeaders, responseType)
            HttpMethod.DELETE -> deleteNB(path, convertedHeaders, responseType)
            HttpMethod.PUT -> putNB(path, request, convertedHeaders, responseType)
            HttpMethod.PATCH -> patchNB(path, request, convertedHeaders, responseType)
            else -> throw BluePrintProcessorException("Unsupported methodType($methodType)")
        }
    }

    protected fun strRequest(request: Any): String {
        return when (request) {
            is String -> request.toString()
            is JsonNode -> request.toString()
            else -> JacksonUtils.getJson(request)
        }
    }

    protected fun <T> getResponse(it: InputStream, responseType: Class<T>): T {
        return if (responseType == String::class.java) {
            IOUtils.toString(it, Charset.defaultCharset()) as T
        } else {
            JacksonUtils.readValue(it, responseType)!!
        }
    }

    protected fun basicHeaders(headers: Map<String, String>?):
        Array<BasicHeader> {
            val basicHeaders = mutableListOf<BasicHeader>()
            defaultHeaders().forEach { (name, value) ->
                basicHeaders.add(BasicHeader(name, value))
            }
            headers?.forEach { name, value ->
                basicHeaders.add(BasicHeader(name, value))
            }
            return basicHeaders.toTypedArray()
        }

    // Non Blocking Rest Implementation
    suspend fun httpClientNB(): CloseableHttpClient {
        return httpClient()
    }

    open fun verifyAdditionalHeaders(): Map<String, String> {
        return verifyAdditionalHeaders(getRestClientProperties())
    }

    open fun verifyAdditionalHeaders(restClientProperties: RestClientProperties): Map<String, String> {
        val customHeaders: MutableMap<String, String> = mutableMapOf()
        // Extract additionalHeaders from the requestProperties and
        // throw an error if HttpHeaders.AUTHORIZATION key (headers are case-insensitive)
        restClientProperties.additionalHeaders?.let {
            if (it.keys.map { k -> k.toLowerCase().trim() }.contains(HttpHeaders.AUTHORIZATION.toLowerCase())) {
                val errMsg = "Error in definition of endpoint ${restClientProperties.url}." +
                    " User-supplied \"additionalHeaders\" cannot contain AUTHORIZATION header with" +
                    " auth-type \"${RestLibConstants.TYPE_BASIC_AUTH}\""
                WebClientUtils.log.error(errMsg)
                throw BluePrintProcessorException(errMsg)
            } else {
                customHeaders.putAll(it)
            }
        }
        return customHeaders
    }
}
