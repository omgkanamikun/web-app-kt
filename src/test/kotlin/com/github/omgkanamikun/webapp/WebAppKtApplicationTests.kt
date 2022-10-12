package com.github.omgkanamikun.webapp

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * @author Vladyslav Kondratenko, omgkanamikun@gmail.com
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebAppKtApplicationTests {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
        private const val URL = "/app"
        private const val COMPANY_QUERY_PARAM = "?content=company"
        private const val PERSON_QUERY_PARAM = "?content=person"
        private const val FIRST_NAME = "firstName"
        private const val DOMAIN = "domain"
    }

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun testPeronRequest() {
        val result = webTestClient.get()
            .uri("$URL$PERSON_QUERY_PARAM")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult()

        logger.info("result: {}", result.responseBody)

        assertTrue(result.responseBody!!.contains(FIRST_NAME))
        assertFalse(result.responseBody!!.contains(DOMAIN))
    }

    @Test
    fun testCompanyRequest() {
        val result = webTestClient.get()
            .uri("$URL$COMPANY_QUERY_PARAM")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult()

        logger.info("result: {}", result.responseBody)

        assertFalse(result.responseBody!!.contains(FIRST_NAME))
        assertTrue(result.responseBody!!.contains(DOMAIN))
    }

    @Test
    fun testRequestWithoutPathParams() {
        webTestClient.get()
            .uri(URL)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }
}
