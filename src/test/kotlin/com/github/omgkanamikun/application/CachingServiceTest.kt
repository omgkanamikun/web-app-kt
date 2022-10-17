package com.github.omgkanamikun.application

import com.github.omgkanamikun.application.model.Entity
import com.github.omgkanamikun.application.service.CachingService
import com.github.omgkanamikun.application.util.randomNumberBounded
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.stream.IntStream

/**
 * @author Vlad Kondratenko, email: omgkanamikun@gmail.com
 * @since 13/10/2022
 */
@SpringBootTest
internal class CachingServiceTest {

    @Autowired
    private lateinit var cachingService: CachingService

    @BeforeEach
    fun setUp() {
        assertNotNull(cachingService)
    }

    @AfterEach
    fun tearDown() {
        cachingService.stats().subscribe {
            logger.info("stats after: $it")
        }
    }

    @Test
    fun cacheTest() {
        //given
        val keysToPersonsMap = IntStream.generate { randomNumberBounded() }
            .limit(89)
            .mapToObj { Pair(it.toString(), testPerson) }
            .toList()
            .associate { it.first to it.second }

        //when
        cachingService.putAll(keysToPersonsMap)

        //then
        cachingService.getIfPresent(keysToPersonsMap.keys.toList())
            .buffer(225)
            .subscribe {
                logger.info("size created: ${keysToPersonsMap.size}, size from cache: ${it.size}")
                assertEquals(keysToPersonsMap.size, it.size)
            }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)

        val testPerson = Entity.Person("test", "test", "test", "test")
    }
}