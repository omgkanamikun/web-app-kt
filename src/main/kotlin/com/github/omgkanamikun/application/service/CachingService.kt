package com.github.omgkanamikun.application.service

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.stats.CacheStats
import com.github.omgkanamikun.application.handler.model.Entity
import com.github.omgkanamikun.application.util.fairy
import com.github.omgkanamikun.application.util.generateId
import com.github.omgkanamikun.application.util.randomNumberBounded
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.annotation.PostConstruct

/**
 * @author Vlad Kondratenko, email: omgkanamikun@gmail.com
 * @since 17/10/2022
 */
class CachingService(
    private val cache: Cache<String, Pair<Entity.Person, Entity.Company>>,
    private val cacheSize: String
) {

    private val ids: MutableList<String> = mutableListOf()

    @PostConstruct
    @Scheduled(fixedRate = 14, timeUnit = TimeUnit.SECONDS)
    fun setUp() {
        ids.clear()

        IntStream.iterate(0, intGenerator())
            .limit(cacheSize.toLong())
            .mapToObj {
                createEntities()
            }
            .flatMap { Stream.of(it) }
            .forEach { tuple2 ->
                tuple2.doOnNext { element ->
                    logger.debug("cache population tick")
                    val id = element.t1
                    val entities = element.t2
                    cache.put(id, Pair(entities.t1, entities.t2))
                }.subscribe()
            }

        logger.info("cache was populated")
    }

    private fun intGenerator() = { number: Int -> number + 1 }

    fun stats(): Mono<CacheStats> = Mono.justOrEmpty(cache.stats())

    fun putAll(allElements: Map<String, Pair<Entity.Person, Entity.Company>>) = cache.putAll(allElements)

    fun getRandomPerson(): Mono<Entity.Person> {
        return Flux.from(pickRandomIdFromList())
            .flatMap { getIfPresent(it) }
            .map { it.first }
            .next()
    }

    fun getRandomCompany(): Mono<Entity.Company> {
        return Flux.from(pickRandomIdFromList())
            .flatMap { getIfPresent(it) }
            .map { it.second }
            .next()
    }

    fun getIfPresent(keys: List<String>): Flux<MutableMap<String, Pair<Entity.Person, Entity.Company>>> {
        return Flux.create { sink ->
            val allPresent = cache.getAllPresent(keys)
            repeat(allPresent.size) { sink.next(allPresent) }
        }
    }

    private fun getIfPresent(key: String): Mono<Pair<Entity.Person, Entity.Company>> {
        return Mono.justOrEmpty(cache.getIfPresent(key))
    }

    private fun createEntities(): Mono<Tuple2<String, Tuple2<Entity.Person, Entity.Company>>> {

        val idMono = Mono.just(generateId())
            .doOnNext {
                ids.add(it)
            }

        val personMono = Mono.just(fairy().person())
            .map {
                Entity.Person(
                    "P${generateId()}",
                    it.firstName,
                    it.lastName,
                    it.dateOfBirth.toString(),
                    it.nationality.name
                )
            }

        val companyMono = Mono.just(fairy().company())
            .map {
                Entity.Company(
                    "C${generateId()}",
                    it.name,
                    it.domain,
                    it.email
                )
            }

        return idMono.zipWith(personMono.zipWith(companyMono))
    }

    private fun pickRandomIdFromList(): Mono<String> {
        return Mono.just(ids[randomNumberBounded(ids.size)])
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
