package com.project3.domain.member.service

import com.project3.domain.member.entity.Member
import com.project3.domain.member.repository.MemberRepository
import com.project3.global.exception.ServiceException
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthService(
        private val memberRepository: MemberRepository,
        private val authTokenService: AuthTokenService
) {

    fun getMemberByAccessToken(accessToken: String): Optional<Member> {
        val payload = authTokenService.getPayload(accessToken) ?: return Optional.empty()

        val id = payload["id"] as Long
        val email = payload["email"] as String

        return Optional.of(
                Member().apply {
                    this.id = id
                    this.email = email
                }
        )
    }

    fun getMemberByRefreshToken(refreshToken: String): Optional<Member> {
        val payload = authTokenService.getPayload(refreshToken) ?: return Optional.empty()
        val id = payload["id"] as Long

        return memberRepository.findById(id)
    }

    fun getMemberByRefreshTokenOrThrow(refreshToken: String): Member {
        return getMemberByRefreshToken(refreshToken)
                .orElseThrow { ServiceException("401", "유효하지 않은 리프레시 토큰이거나 회원을 찾을 수 없습니다.") }
    }

    fun genAccessToken(member: Member): String {
        return authTokenService.genAccessToken(member)
    }

    fun genRefreshToken(member: Member): String {
        return authTokenService.genRefreshToken(member.id!!)
    }

    fun validateOwner(actorId: Long, memberId: Long, errorMessage: String) {
        if (actorId != memberId) {
            throw ServiceException("403", errorMessage)
        }
    }
}
