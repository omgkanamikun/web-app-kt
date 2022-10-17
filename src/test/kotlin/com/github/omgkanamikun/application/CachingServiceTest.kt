package com.github.omgkanamikun.application

import com.github.omgkanamikun.application.CachingService.Entity.Person
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
        logger.info("stats after: ${cachingService.stats()}")
    }

    @Test
    fun cacheTest() {
        //given
        val keysToPersonsMap = IntStream.generate { randomNumberBounded() }
            .limit(89)
            .mapToObj {
                Pair(it.toString(), person)
            }
            .toList().associate { it.first to it.second }

        //when
        cachingService.putAll(keysToPersonsMap)

        //then
        val sizeInCache = cachingService.getIfPresent(keysToPersonsMap.keys.toList()).size

        assertEquals(keysToPersonsMap.size, sizeInCache)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)

        val person = Person("test", "test", "test", "test")

        private fun randomNumberBounded(): Int = (Math.random() * 100).toInt()
    }
}