package com.project2.global.security

import com.project2.domain.member.entity.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.core.user.OAuth2User

class SecurityUser(
        val id: Long,
        email: String,
        authorities: Collection<GrantedAuthority>
) : User(email, "", authorities), OAuth2User {

    constructor(member: Member) : this(member.id!!, member.email, member.getAuthorities())

    override fun <A> getAttribute(name: String): A? {
        return attributes[name] as A? // return OAuth2User.super.getAttribute(name); <- 기존 java 코드
    }

    override fun getAttributes(): Map<String, Any> {
        return emptyMap()
    }

    override fun getName(): String {
        return username
    }
}
