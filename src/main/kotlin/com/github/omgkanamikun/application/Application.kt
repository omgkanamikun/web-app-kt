package com.github.omgkanamikun.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * @author Vlad Kondratenko, email: omgkanamikun@gmail.com
 * @since 13/10/2022
 */
@SpringBootApplication
@EnableScheduling
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
