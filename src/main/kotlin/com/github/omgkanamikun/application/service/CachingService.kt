package com.github.omgkanamikun.application.service

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.stats.CacheStats
import com.github.omgkanamikun.application.model.Entity
import com.github.omgkanamikun.application.util.fairy
import com.github.omgkanamikun.application.util.randomNumberBounded
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.annotation.PostConstruct

/**
 * @author Vlad Kondratenko, email: omgkanamikun@gmail.com
 * @since 17/10/2022
 */
class CachingService(private val cache: Cache<String, Entity>) {

    @PostConstruct
    fun setUp() {
        IntStream.generate { randomNumberBounded() }
            .limit(100)
            .mapToObj(::createPersonOrCompany)
            .flatMap { Stream.of(it) }
            .toList()
            .forEach(Mono<out Tuple2<out Entity, String>>::subscribe)

        logger.info("cache was populated")
    }

    fun stats(): Mono<CacheStats> = Mono.justOrEmpty(cache.stats())

    fun putAll(allElements: Map<String, Entity>) = cache.putAll(allElements)

    fun getRandomPerson(): Mono<Entity> {
        return Flux.from(pickRandomIdFromList())
            .flatMap { getIfPresent(it) }
            .filter { it is Entity.Person }
            .next()
    }

    fun getRandomCompany(): Mono<Entity> {
        return Flux.from(pickRandomIdFromList())
            .flatMap { getIfPresent(it) }
            .filter { it is Entity.Company }
            .next()
    }

    fun getIfPresent(keys: List<String>): Flux<Entity> {
        return Flux.create {
            val allPresent = cache.getAllPresent(keys)
            allPresent.forEach { (_, u) ->
                it.next(u)
            }
        }
    }

    private fun getIfPresent(key: String): Mono<Entity> {
        return Mono.justOrEmpty(cache.getIfPresent(key))
    }

    private fun createPersonOrCompany(it: Int): Mono<out Tuple2<out Entity, String>> {
        return if (it % 2 == 0) {
            createPersonInCache()
        } else createCompanyInCache()
    }

    private fun createPersonInCache(): Mono<Tuple2<Entity.Person, String>> {
        return Mono.just(fairy().person())
            .map {
                Entity.Person(
                    it.firstName, it.lastName, it.dateOfBirth.toString(), it.nationality.name
                )
            }
            .zipWith(Mono.just(randomNumberBounded().toString()))
            .doOnNext {
                cache.put(it.t2, it.t1)
            }
    }

    private fun createCompanyInCache(): Mono<Tuple2<Entity.Company, String>> {
        return Mono.just(fairy().company())
            .map { Entity.Company(it.name, it.domain, it.email) }
            .zipWith(Mono.just(randomNumberBounded().toString()))
            .doOnNext {
                cache.put(it.t2, it.t1)
            }
    }

    private fun pickRandomIdFromList(): Mono<String> {
        return Mono.just(randomNumberBounded().toString())
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}