package com.github.omgkanamikun.application

import com.devskiller.jfairy.Fairy
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.stats.CacheStats
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream


/**
 * @author Vlad Kondratenko, email: omgkanamikun@gmail.com
 * @since 13/10/2022
 */
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Service
class FairyService(val cachingService: CachingService) {

    fun getPerson(): CachingService.Entity = cachingService.getRandomPerson()

    fun getCompany(): CachingService.Entity = cachingService.getRandomCompany()
}

class CachingService(private val cache: Cache<String, Entity>) {

    private val inCacheKeysList = mutableListOf<String>()

    init {
        logger.info("cache was initialized, stats ${stats()}")

        val count = IntStream.range(0, 251).mapToObj {
            createPersonOrCompany(it)
        }.count()

        logger.info("cache populated by $count objects")
    }

    fun stats(): CacheStats = cache.stats()

    private fun createPersonOrCompany(it: Int) {
        if (it % 2 == 0) createCompanyInCache() else createPersonInCache()
    }

    fun putAll(allElements: Map<String, Entity>) = cache.putAll(allElements)

    fun getIfPresent(keys: List<String>): MutableMap<String, Entity> = cache.getAllPresent(keys)

    fun getRandomPerson(): Entity {
        while (true) {
            val key = pickRandomIdFromList()
            if (getIfPresent(key) is Entity.Person) {
                return getIfPresent(key)!!
            } else {
                continue
            }
        }
    }

    fun getRandomCompany(): Entity {
        while (true) {
            val key = pickRandomIdFromList()
            if (getIfPresent(key) is Entity.Company) {
                return getIfPresent(key)!!
            } else {
                continue
            }
        }
    }

    private fun getIfPresent(key: String) = cache.getIfPresent(key)

    private fun createPersonInCache() {
        val original = fairy.person()

        val person = Entity.Person(
            original.firstName, original.lastName, original.dateOfBirth.toString(), original.nationality.name
        )
        val key = randomNumberBounded().toString()

        inCacheKeysList.add(key)

        cache.put(key, person)
    }

    private fun createCompanyInCache() {
        val original = fairy.company()

        val company = Entity.Company(original.name, original.domain, original.email)
        val key = randomNumberBounded().toString()

        inCacheKeysList.add(key)

        cache.put(key, company)
    }

    private fun pickRandomIdFromList(): String {
        if (inCacheKeysList.size <= 0) throw RuntimeException("keys is empty!")
        return (randomNumberBounded() % inCacheKeysList.size).toString()
    }

    sealed interface Entity {
        data class Person(
            val firstName: String, val lastName: String, val dateOfBirth: String, val nationality: String
        ) : Entity

        data class Company(
            val name: String, val domain: String, val email: String
        ) : Entity
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
        private val fairy: Fairy = Fairy.create()
    }
}

@Component
class FairyRequestHandler(val service: FairyService, val objectMapper: ObjectMapper) {

    fun handlePersonRequest(request: ServerRequest): Mono<ServerResponse> {
        logger.info("handling person request")

        val personAsJson = objectMapper.writeValueAsString(service.getPerson())

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(personAsJson)
    }

    fun handleCompanyRequest(request: ServerRequest): Mono<ServerResponse> {
        logger.info("handling company request")

        val companyAsJson = objectMapper.writeValueAsString(service.getCompany())

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(companyAsJson)
    }

    fun handleInfoRequest(serverRequest: ServerRequest): Mono<ServerResponse> {

        logger.info("handling company request")

        return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).bodyValue(RESPONSE_BODY_TEXT_INFO)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
        private const val RESPONSE_BODY_TEXT_INFO = "query params: ?content = can be person or company"
    }
}

@Configuration
class ApplicationConfig {

    @Value("\${cache.size}")
    private lateinit var cacheSize: String

    @Bean(name = ["cachingService"])
    fun cachingService(): CachingService {
        return CachingService(
            Caffeine.newBuilder()
                .maximumSize(cacheSize.toLong())
                .initialCapacity(cacheSize.toInt())
                .expireAfterWrite(31, TimeUnit.SECONDS)
                .build()
        )
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
        ).andRoute(
            RequestPredicates.GET(URL),
            fairyRequestHandler::handleInfoRequest
        )
    }

    companion object {
        private const val URL = "/app"
    }
}

private fun randomNumberBounded() = (Math.random() * 100).toInt()