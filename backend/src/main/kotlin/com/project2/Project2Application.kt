package com.project2

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class Project2Application

fun main(args: Array<String>) {
    SpringApplication.run(Project2Application::class.java, *args)
}
