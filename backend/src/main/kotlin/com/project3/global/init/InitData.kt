package com.project3.global.init

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class InitData {

    @PostConstruct
    fun init() {
    }
}