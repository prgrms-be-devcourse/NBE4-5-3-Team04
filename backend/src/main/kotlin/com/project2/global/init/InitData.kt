package com.project2.global.init

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class InitData {

    @PostConstruct
    fun init() {
    }
}