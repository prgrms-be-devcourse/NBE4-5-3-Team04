package com.project2.domain.member.service

import com.project2.domain.member.entity.Member
import com.project2.global.util.Ut
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AuthTokenService {

    @Value("\${custom.jwt.secret-key}")
    private lateinit var keyString: String

    @Value("\${custom.jwt.access-token-expire-seconds}")
    private var accessTokenExpireSeconds: Int = 0

    @Value("\${custom.jwt.refresh-token-expire-seconds}")
    private var refreshTokenExpireSeconds: Int = 0

    fun genAccessToken(member: Member): String {
        return Ut.Jwt.createToken(
                keyString,
                accessTokenExpireSeconds,
                mapOf("id" to member.id, "email" to member.email)
        )
    }

    fun genRefreshToken(id: Long): String {
        return Ut.Jwt.createToken(
                keyString,
                refreshTokenExpireSeconds,
                mapOf("id" to id)
        )
    }

    fun getPayload(token: String): Map<String, Any>? {
        if (!Ut.Jwt.isValidToken(keyString, token)) return null

        val payload = Ut.Jwt.getPayload(keyString, token)
        val id = (payload["id"] as Number).toLong()
        val email = payload["email"] as? String ?: ""

        return mapOf("id" to id, "email" to email)
    }
}