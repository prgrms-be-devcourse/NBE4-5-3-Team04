package com.project3

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class Project3Application

fun main(args: Array<String>) {
    SpringApplication.run(Project3Application::class.java, *args)
}
