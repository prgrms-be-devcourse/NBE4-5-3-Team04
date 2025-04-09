package com.project2.global.security

import com.project2.domain.member.enums.Provider
import com.project2.domain.member.service.MemberService
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOauth2UserService(
        private val memberService: MemberService
) : DefaultOAuth2UserService() {

    @Transactional
    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        val providerType = userRequest.clientRegistration.registrationId
        val provider = if (providerType == "naver") Provider.NAVER else Provider.GOOGLE

        val attributes = oAuth2User.attributes

        val (nickname, profileImage, email) = if (provider == Provider.NAVER) {
            val response = attributes["response"] as Map<*, *>
            Triple(
                    response["nickname"] as String,
                    response["profile_image"] as String,
                    response["email"] as String
            )
        } else {
            Triple(
                    attributes["name"] as String,
                    attributes["picture"] as String,
                    attributes["email"] as String
            )
        }

        val opMember = memberService.findByEmail(email)

        if (opMember.isPresent) {
            val member = opMember.get()
            member.nickname = nickname
            return SecurityUser(member)
        }

        val member = memberService.signUp(email, nickname, profileImage, provider)
        return SecurityUser(member)
    }
}
