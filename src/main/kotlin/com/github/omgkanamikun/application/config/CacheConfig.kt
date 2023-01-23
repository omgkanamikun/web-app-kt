package com.github.omgkanamikun.application.config

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.omgkanamikun.application.service.CachingService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

/**
 * @author Vlad Kondratenko, email: omgkanamikun@gmail.com
 * @since 17/10/2022
 */
@Configuration
class CacheConfig {

    @Value("\${cache.size}")
    private lateinit var cacheSize: String

    @Bean(name = ["cachingService"])
    fun cachingService(): CachingService {
        return CachingService(
            Caffeine.newBuilder()
                .maximumSize(cacheSize.toLong())
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build(),
            cacheSize
        )
    }
}
