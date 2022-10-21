package com.github.omgkanamikun.application.config

import com.github.omgkanamikun.application.handler.HttpRequestHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse

/**
 * @author Vlad Kondratenko, email: omgkanamikun@gmail.com
 * @since 17/10/2022
 */
@Configuration
class RouterFunctionConfig {

    @Bean
    fun routerFunction(handler: HttpRequestHandler): RouterFunction<ServerResponse> {
        return RouterFunctions.route(
            RequestPredicates.GET(URL)
                .and(RequestPredicates.queryParam("content", "person")),
            handler::handlePersonRequest
        ).andRoute(
            RequestPredicates.GET(URL)
                .and(RequestPredicates.queryParam("content", "company")),
            handler::handleCompanyRequest
        ).andRoute(
            RequestPredicates.GET(URL),
            handler::handleInfoRequest
        ).andRoute(
            RequestPredicates.all(),
            handler::handleBadRequest
        )
    }

    companion object {
        private const val URL = "/app"
    }
}
