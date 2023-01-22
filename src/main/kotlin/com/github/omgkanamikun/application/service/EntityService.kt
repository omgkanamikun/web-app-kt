package com.github.omgkanamikun.application.service

import com.github.omgkanamikun.application.handler.model.Entity
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * @author Vlad Kondratenko, email: omgkanamikun@gmail.com
 * @since 17/10/2022
 */
@Service
class EntityService(val cachingService: CachingService) {

    fun getPerson(): Mono<Entity.Person> {
        return cachingService.getRandomPerson()
    }

    fun getCompany(): Mono<Entity.Company> {
        return cachingService.getRandomCompany()
    }
}
