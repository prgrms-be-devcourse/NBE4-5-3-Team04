package com.project2.global.security

import com.project2.domain.member.entity.Member
import com.project2.domain.member.service.MemberService
import com.project2.global.exception.ServiceException
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
class Rq(
        private val request: HttpServletRequest,
        private val response: HttpServletResponse,
        private val memberService: MemberService,
        @Value("\${custom.url.domain}") private val siteDomain: String
) {

    fun setLogin(actor: Member) {
        val user: UserDetails = SecurityUser(
                actor.id!!,
                actor.email,
                listOf(SimpleGrantedAuthority("ROLE_USER"))
        )
        SecurityContextHolder.getContext().authentication =
                UsernamePasswordAuthenticationToken(user, null, user.authorities)
    }

    fun getActor(): Member {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
                ?: throw ServiceException("401-2", "로그인이 필요합니다.")

        val principal = authentication.principal
        if (principal !is SecurityUser) {
            throw ServiceException("401-3", "잘못된 인증 정보입니다")
        }

        return Member.builder()
                .id(principal.id)
                .email(principal.username)
                .build()
    }

    fun getHeader(name: String): String? {
        return request.getHeader(name)
    }

    fun getValueFromCookie(name: String): String? {
        val cookies = request.cookies ?: return null
        return cookies.firstOrNull { it.name == name }?.value
    }

    fun setHeader(name: String, value: String) {
        response.setHeader(name, value)
    }

    fun addCookie(name: String, value: String, isHttpOnly: Boolean) {
        val accessTokenCookie = Cookie(name, value).apply {
            domain = siteDomain
            path = "/"
            this.isHttpOnly = isHttpOnly
            secure = true
            setAttribute("SameSite", "Strict")
        }
        response.addCookie(accessTokenCookie)
    }

    fun getRealActor(actor: Member): Member {
        return memberService.findById(actor.id!!).orElseThrow()
    }

    fun removeCookie(name: String) {
        val cookie = Cookie(name, null).apply {
            domain = siteDomain
            path = "/"
            isHttpOnly = true
            secure = true
            setAttribute("SameSite", "Strict")
            maxAge = 0
        }
        response.addCookie(cookie)
    }
}
