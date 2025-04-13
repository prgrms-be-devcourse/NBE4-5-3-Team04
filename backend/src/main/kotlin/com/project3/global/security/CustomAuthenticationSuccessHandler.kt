package com.project3.global.security

import com.project3.domain.member.entity.Member
import com.project3.domain.member.service.AuthService
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class CustomAuthenticationSuccessHandler(
        private val rq: Rq,
        private val authService: AuthService
) : AuthenticationSuccessHandler {

    @Value("\${custom.url.front-url}")
    private lateinit var siteFrontUrl: String

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationSuccess(
            request: HttpServletRequest,
            response: HttpServletResponse,
            authentication: Authentication
    ) {
        val session: HttpSession = request.session
        var redirectUrl = session.getAttribute("redirectUrl") as String?
        if (redirectUrl == null) {
            redirectUrl = siteFrontUrl
        }
        session.removeAttribute("redirectUrl")

        val member: Member = rq.getActor()
        val accessToken = authService.genAccessToken(member)
        val refreshToken = authService.genRefreshToken(member)

        rq.addCookie("accessToken", accessToken, false)
        rq.addCookie("refreshToken", refreshToken, true)

        response.sendRedirect(redirectUrl)
    }
}
