package com.project2.global.security

import com.project2.domain.member.entity.Member
import com.project2.domain.member.service.AuthService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class CustomAuthenticationFilter(
        private val rq: Rq,
        private val authService: AuthService
) : OncePerRequestFilter() {

    private fun isAuthorizationHeader(): Boolean {
        val authorizationHeader = rq.getHeader("Authorization") ?: return false
        return authorizationHeader.trim().startsWith("Bearer ")
    }

    private data class AuthToken(val accessToken: String, val refreshToken: String)

    private fun getAuthTokenFromRequest(): AuthToken? {
        if (isAuthorizationHeader()) {
            val authorizationHeader = rq.getHeader("Authorization") ?: return null
            val accessToken = authorizationHeader.substring("Bearer ".length)
            return AuthToken(accessToken, "")
        }

        val accessToken = rq.getValueFromCookie("accessToken")
        val refreshToken = rq.getValueFromCookie("refreshToken")

        if (accessToken == null && refreshToken == null) {
            return null
        }

        return AuthToken(accessToken ?: "", refreshToken ?: "")
    }

    private fun getMemberByAccessToken(accessToken: String, refreshToken: String): Member? {
        val opAccMember = authService.getMemberByAccessToken(accessToken)
        if (opAccMember.isPresent) {
            return opAccMember.get()
        }

        if (refreshToken.isEmpty()) {
            return null
        }

        val opRefMember = authService.getMemberByRefreshToken(refreshToken)
        if (opRefMember.isEmpty) {
            return null
        }

        val newAccessToken = authService.genAccessToken(opRefMember.get())
        rq.addCookie("accessToken", newAccessToken, false)

        return opRefMember.get()
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
    ) {
        val url = request.requestURI

        if (listOf("/api/members/logout", "/api/members/login").contains(url)) {
            filterChain.doFilter(request, response)
            return
        }

        val tokens = getAuthTokenFromRequest()

        if (tokens == null || (tokens.accessToken.isEmpty() && tokens.refreshToken.isEmpty())) {
            filterChain.doFilter(request, response)
            return
        }

        val actor = getMemberByAccessToken(tokens.accessToken, tokens.refreshToken)

        if (actor == null) {
            response.contentType = "application/json;charset=UTF-8"
            response.characterEncoding = "UTF-8"
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("{\"message\": \"토큰이 만료되었습니다.\"}")
            response.writer.flush()
            return
        }

        rq.setLogin(actor)
        filterChain.doFilter(request, response)
    }
}
