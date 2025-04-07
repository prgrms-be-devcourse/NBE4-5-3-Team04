package com.project2.global.security

import com.project2.domain.member.entity.Member
import com.project2.domain.member.entity.Member.Companion.builder
import com.project2.domain.member.service.MemberService
import com.project2.global.exception.ServiceException
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.List

// Request, Response, Session, Cookie, Header
@Component
@RequiredArgsConstructor
class Rq {
    private val request: HttpServletRequest? = null
    private val response: HttpServletResponse? = null
    private val memberService: MemberService? = null

    @Value("\${custom.url.domain}")
    private val siteDomain: String? = null

    fun setLogin(actor: Member) {
        // 유저 정보 생성

        val user: UserDetails = SecurityUser(
            actor.id!!, actor.email,
            List.of(SimpleGrantedAuthority("ROLE_USER"))
        )

        // 인증 정보 저장소
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(user, null, user.authorities)
    }

    val actor: Member
        get() {
            val authentication =
                SecurityContextHolder.getContext().authentication
                    ?: throw ServiceException("401-2", "로그인이 필요합니다.")

            val principal = authentication.principal as? SecurityUser
                ?: throw ServiceException("401-3", "잘못된 인증 정보입니다")

            val user = principal

            return builder()
                .id(user.id)
                .email(user.username)
                .build()
        }

    fun getHeader(name: String?): String {
        return request!!.getHeader(name)
    }

    fun getValueFromCookie(name: String): String? {
        val cookies = request!!.cookies ?: return null

        for (cookie in cookies) {
            if (cookie.name == name) {
                return cookie.value
            }
        }

        return null
    }

    fun setHeader(name: String?, value: String?) {
        response!!.setHeader(name, value)
    }

    fun addCookie(name: String?, value: String?, isHttpOnly: Boolean) {
        val accsessTokenCookie = Cookie(name, value)

        accsessTokenCookie.domain = siteDomain
        accsessTokenCookie.path = "/"
        accsessTokenCookie.isHttpOnly = isHttpOnly
        accsessTokenCookie.secure = true
        accsessTokenCookie.setAttribute("SameSite", "Strict")

        response!!.addCookie(accsessTokenCookie)
    }

    fun getRealActor(actor: Member): Member {
        return memberService!!.findById(actor.id).get()
    }

    fun removeCookie(name: String?) {
        // 원칙적으로 쿠키를 서버에서 삭제하는 것은 불가능.

        val cookie = Cookie(name, null)
        cookie.domain = siteDomain
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.secure = true
        cookie.setAttribute("SameSite", "Strict")
        cookie.maxAge = 0

        response!!.addCookie(cookie)
    }
}