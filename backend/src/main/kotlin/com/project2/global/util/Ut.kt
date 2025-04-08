package com.project2.global.util

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*
import javax.crypto.SecretKey

object Ut {

    object Json {
        private val objectMapper = ObjectMapper()

        @JvmStatic
        fun toString(obj: Any): String {
            return try {
                objectMapper.writeValueAsString(obj)
            } catch (e: JsonProcessingException) {
                throw RuntimeException(e)
            }
        }
    }

    object Jwt {

        @JvmStatic
        fun createToken(keyString: String, expireSeconds: Int, claims: Map<String, Any>): String {
            val secretKey: SecretKey = Keys.hmacShaKeyFor(keyString.toByteArray())
            val issuedAt = Date()
            val expiration = Date(issuedAt.time + 1000L * expireSeconds)

            return Jwts.builder()
                    .claims(claims)
                    .issuedAt(issuedAt)
                    .expiration(expiration)
                    .signWith(secretKey)
                    .compact()
        }

        @JvmStatic
        fun isValidToken(keyString: String, token: String): Boolean {
            return try {
                val secretKey: SecretKey = Keys.hmacShaKeyFor(keyString.toByteArray())
                Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parse(token)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        @JvmStatic
        fun getPayload(keyString: String, jwtStr: String): Map<String, Any> {
            val secretKey: SecretKey = Keys.hmacShaKeyFor(keyString.toByteArray())
            val payload = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwtStr)
                    .payload

            @Suppress("UNCHECKED_CAST")
            return payload as Map<String, Any>
        }
    }
}