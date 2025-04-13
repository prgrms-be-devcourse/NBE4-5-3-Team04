package com.project3.global.app

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    companion object {
        @JvmStatic
        lateinit var objectMapper: ObjectMapper
            private set
    }

    @Autowired
    fun setObjectMapper(objectMapper: ObjectMapper) {
        AppConfig.objectMapper = objectMapper
    }
}
