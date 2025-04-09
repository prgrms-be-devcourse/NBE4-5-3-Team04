package com.project2.global.security

import com.project2.global.dto.RsData
import com.project2.global.util.Ut
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.context.NullSecurityContextRepository
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
        private val customAuthenticationFilter: CustomAuthenticationFilter,
        private val customAuthorizationRequestResolver: CustomAuthorizationRequestResolver,
        private val customAuthenticationSuccessHandler: CustomAuthenticationSuccessHandler,
        private val customLogoutSuccessHandler: CustomLogoutSuccessHandler,
        @Value("\${custom.url.front-url}") private val siteFrontUrl: String
) {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { auth ->
            auth.requestMatchers(
                    "/h2-console/**", "/auth/**", "/oauth2/**", "/v3/api-docs/**",
                    "/api/members/login", "/api/members/logout", "/login", "/error",
                    "/uploads/**", "/_next/image", "/swagger-ui/**"
            ).permitAll()
                    .anyRequest().authenticated()
        }
                .csrf { it.disable() }
                .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
                .securityContext { it.securityContextRepository(NullSecurityContextRepository()) }
                .oauth2Login { oauth2 ->
                    oauth2.authorizationEndpoint {
                        it.authorizationRequestResolver(customAuthorizationRequestResolver)
                    }
                    oauth2.successHandler(customAuthenticationSuccessHandler)
                }
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
                .logout { logout ->
                    logout.logoutUrl("/api/members/logout")
                            .logoutSuccessHandler(customLogoutSuccessHandler)
                }
                .headers { headers ->
                    headers.addHeaderWriter(
                            XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)
                    )
                }
                .exceptionHandling { exceptionHandling ->
                    exceptionHandling.authenticationEntryPoint { _, response, _ ->
                        response.contentType = "application/json;charset=UTF-8"
                        response.status = 401
                        response.writer.write(Ut.Json.toString(RsData<Unit>("401", "잘못된 인증키입니다.")))
                    }.accessDeniedHandler { _, response, _ ->
                        response.contentType = "application/json;charset=UTF-8"
                        response.status = 403
                        response.writer.write(Ut.Json.toString(RsData<Unit>("403", "접근 권한이 없습니다.")))
                    }
                }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf("https://cdpn.io", siteFrontUrl)
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
            allowCredentials = true
            allowedHeaders = listOf("*")
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/api/**", configuration)
            registerCorsConfiguration("/uploads/**", configuration)
        }
    }
}
