package com.github.omgkanamikun.webapp

import com.devskiller.jfairy.Fairy
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono

/**
 * @author Vladyslav Kondratenko, omgkanamikun@gmail.com
 */
@SpringBootApplication
class WebAppKtApplication

fun main(args: Array<String>) {
    runApplication<WebAppKtApplication>(*args)
}

@Component
class FairyService(val objectMapper: ObjectMapper) {

    companion object {
        private val fairy: Fairy = Fairy.create()
    }

    fun getPersonAsJson(): String = objectMapper.writeValueAsString(createPerson())

    fun getCompanyAsJson(): String = objectMapper.writeValueAsString(createCompany())

    private fun createPerson(): Person {
        val original = fairy.person()
        return Person(original.firstName, original.lastName, original.dateOfBirth.toString(), original.nationality.name)
    }

    private fun createCompany(): Company {
        val original = fairy.company()
        return Company(original.name, original.domain, original.email)
    }

    data class Person(val firstName: String, val lastName: String, val dateOfBirth: String, val nationality: String)
    data class Company(val name: String, val domain: String, val email: String)
}

@Component
class FairyRequestHandler(val service: FairyService) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun handlePersonRequest(request: ServerRequest): Mono<ServerResponse> {
        logger.info("handling person request")
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(service.getPersonAsJson())
    }

    fun handleCompanyRequest(request: ServerRequest): Mono<ServerResponse> {
        logger.info("handling company request")
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(service.getCompanyAsJson())
    }
}

@Configuration
class RequestRouter {

    companion object {
        private const val URL = "/app"
    }

    @Bean
    fun routePersonRequests(fairyRequestHandler: FairyRequestHandler): RouterFunction<ServerResponse> {
        return RouterFunctions.route(
            RequestPredicates.GET(URL)
                .and(RequestPredicates.queryParam("content", "person")),
            fairyRequestHandler::handlePersonRequest
        ).andRoute(
            RequestPredicates.GET(URL)
                .and(RequestPredicates.queryParam("content", "company")),
            fairyRequestHandler::handleCompanyRequest
        )
    }
}
