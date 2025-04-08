package com.project2.global.springdocs

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
        info = Info(
                title = "API 서버",
                version = "v1"
        )
)
class SpringDocConfig {

    @Bean
    fun groupController(): GroupedOpenApi {
        return GroupedOpenApi.builder()
                .group("controller")
                .pathsToExclude("/api/**")
                .build()
    }
}