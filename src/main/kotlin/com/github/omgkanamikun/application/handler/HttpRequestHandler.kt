package com.github.omgkanamikun.application.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.omgkanamikun.application.service.FairyService
import com.github.omgkanamikun.application.util.emptyCompany
import com.github.omgkanamikun.application.util.emptyPerson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

/**
 * @author Vlad Kondratenko, email: omgkanamikun@gmail.com
 * @since 17/10/2022
 */
@Component
class HttpRequestHandler(val service: FairyService, val objectMapper: ObjectMapper) {

    fun handlePersonRequest(request: ServerRequest): Mono<ServerResponse> {
        logger.info("handling person request")

        return request.toMono()
            .flatMap { service.getPerson() }
            .switchIfEmpty { Mono.just(emptyPerson()) }
            .doOnNext { logger.info("generated $it") }
            .map { objectMapper.writeValueAsString(it) }
            .flatMap {
                logger.info("response json: ${it ?: "null"}")
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(it)
            }
    }

    fun handleCompanyRequest(request: ServerRequest): Mono<ServerResponse> {
        logger.info("handling company request")

        return request.toMono()
            .flatMap { service.getCompany() }
            .switchIfEmpty { Mono.just(emptyCompany()) }
            .doOnNext { logger.info("generated $it") }
            .map { objectMapper.writeValueAsString(it) }
            .flatMap {
                logger.info("response json: ${it ?: "null"}")
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(it)
            }
    }

    fun handleInfoRequest(serverRequest: ServerRequest): Mono<ServerResponse> {
        logger.info("handling info request")

        return serverRequest.toMono()
            .map { Mono.just(RESPONSE_BODY_TEXT_INFO) }
            .flatMap {
                logger.info("response: ${it ?: "null"}")
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body<String>(it)
            }
    }

    companion object {
        private const val RESPONSE_BODY_TEXT_INFO = "query params: ?content = can be person or company"

        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}