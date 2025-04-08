package com.project2.global.init

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

@Profile("dev")
@Configuration
class DevInitData {

    @Value("\${custom.url.base-url}")
    private lateinit var siteBaseUrl: String

    @Bean
    fun devApplicationRunner(): ApplicationRunner {
        return ApplicationRunner {
            genApiJsonFile("$siteBaseUrl/v3/api-docs", "apiV1.json")
            runCmd(getPlatformSpecificCommand())
        }
    }

    private fun isWindows(): Boolean =
            System.getProperty("os.name").lowercase().contains("win")

    private fun getPlatformSpecificCommand(): List<String> {
        val command =
                "npx --package typescript --package openapi-typescript --package punycode openapi-typescript apiV1.json -o ../frontend/src/lib/backend/schema.d.ts"

        return if (isWindows()) {
            listOf("cmd.exe", "/c", command)
        } else {
            listOf("sh", "-c", command)
        }
    }

    fun runCmd(command: List<String>) {
        try {
            val processBuilder = ProcessBuilder(command)
            processBuilder.redirectErrorStream(true)

            val process = processBuilder.start()

            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                reader.lines().forEach { println(it) }
            }

            val exitCode = process.waitFor()
            println("프로세스 종료 코드: $exitCode")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun genApiJsonFile(url: String, filename: String) {
        val filePath = Path.of(filename)
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build()

        try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) {
                Files.writeString(
                        filePath,
                        response.body(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                )
                println("JSON 데이터가 ${filePath.toAbsolutePath()}에 저장되었습니다.")
            } else {
                System.err.println("오류: HTTP 상태 코드 ${response.statusCode()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}