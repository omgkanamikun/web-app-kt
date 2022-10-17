package com.github.omgkanamikun.application.service

import com.github.omgkanamikun.application.model.Entity
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * @author Vlad Kondratenko, email: omgkanamikun@gmail.com
 * @since 17/10/2022
 */
@Service
class FairyService(val cachingService: CachingService) {

    fun getPerson(): Mono<Entity> {
        return cachingService.getRandomPerson()
    }

    fun getCompany(): Mono<Entity> {
        return cachingService.getRandomCompany()
    }
}