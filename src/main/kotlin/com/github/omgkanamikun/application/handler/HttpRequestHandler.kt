package com.github.omgkanamikun.application.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.omgkanamikun.application.service.EntityService
import com.github.omgkanamikun.application.util.emptyCompany
import com.github.omgkanamikun.application.util.emptyPerson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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
class HttpRequestHandler(val service: EntityService, val mapper: ObjectMapper) {

    @Value("\${application.marker}")
    private lateinit var marker: String

    fun handlePersonRequest(request: ServerRequest): Mono<ServerResponse> {
        return request.toMono()
            .doOnNext { logger.info("handling person request") }
            .flatMap { service.getPerson() }
            .switchIfEmpty { Mono.just(emptyPerson()) }
            .doOnNext { logger.info("generated $it") }
            .map { mapper.writeValueAsString(it) }
            .flatMap {
                logger.info("response json: ${it ?: "null"}")
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(it)
            }
    }

    fun handleCompanyRequest(request: ServerRequest): Mono<ServerResponse> {
        return request.toMono()
            .doOnNext { logger.info("handling company request") }
            .flatMap { service.getCompany() }
            .switchIfEmpty { Mono.just(emptyCompany()) }
            .doOnNext { logger.info("generated $it") }
            .map { mapper.writeValueAsString(it) }
            .flatMap {
                logger.info("response json: ${it ?: "null"}")
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(it)
            }
    }

    fun handleInfoRequest(request: ServerRequest): Mono<ServerResponse> {
        return request.toMono()
            .doOnNext { logger.info("handling info request") }
            .map {
                Mono.just(
                    "Instance marker: $marker\n" +
                            TEXT_INFO_BODY
                )
            }
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body<String>(it)
            }
    }

    fun handleBadRequest(request: ServerRequest): Mono<ServerResponse> {
        return request.toMono()
            .doOnNext { logger.error("handling bad request") }
            .map { Mono.just(BAD_REQUEST_BODY) }
            .flatMap {
                ServerResponse.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body<String>(it)
            }
    }

    companion object {
        private const val TEXT_INFO_BODY = "request query params: ?content = person or company"
        private const val BAD_REQUEST_BODY = "bad request"
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
