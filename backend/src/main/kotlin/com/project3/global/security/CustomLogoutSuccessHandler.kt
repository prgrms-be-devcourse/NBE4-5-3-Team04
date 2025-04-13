package com.project3.global.security

import com.project3.global.dto.RsData
import com.project3.global.util.Ut
import jakarta.servlet.ServletException
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class CustomLogoutSuccessHandler : LogoutSuccessHandler {

    @Value("\${custom.url.domain}")
    private lateinit var siteDomain: String

    @Throws(IOException::class, ServletException::class)
    override fun onLogoutSuccess(
            request: HttpServletRequest,
            response: HttpServletResponse,
            authentication: Authentication?
    ) {
        // 세션 무효화
        val session: HttpSession? = request.getSession(false)
        session?.invalidate()

        // SecurityContext 초기화
        SecurityContextHolder.clearContext()

        // 쿠키 삭제
        removeCookie(response, "accessToken")
        removeCookie(response, "refreshToken")
        removeCookie(response, "JSESSIONID")

        // 로그아웃 성공 응답 반환
        response.contentType = "application/json;charset=UTF-8"
        response.status = HttpServletResponse.SC_OK
        response.writer.write(Ut.Json.toString(RsData<Unit>("200", "로그아웃이 완료되었습니다.")))
        response.writer.flush()
        response.writer.close()
    }

    // 쿠키 삭제 메서드
    private fun removeCookie(response: HttpServletResponse, cookieName: String) {
        val cookie = Cookie(cookieName, null).apply {
            domain = siteDomain
            path = "/"
            isHttpOnly = true
            secure = true
            maxAge = 0 // 쿠키 즉시 삭제
            setAttribute("SameSite", "Strict")
        }
        response.addCookie(cookie)
    }
}
