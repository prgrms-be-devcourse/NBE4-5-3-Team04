package com.project3.domain.member.entity

import com.project3.domain.member.enums.Provider
import com.project3.global.entity.BaseTime
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

@Entity
class Member(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @Column(nullable = false, unique = true)
        var email: String = "",

        @Column(nullable = false, length = 50)
        var nickname: String = "",

        var profileImageUrl: String? = null,

        @Enumerated(EnumType.STRING)
        @Column(columnDefinition = "VARCHAR(20)")
        var provider: Provider? = null
) : BaseTime() {

    fun getAuthorities(): Collection<GrantedAuthority> =
            getMemberAuthoritiesAsString().map { SimpleGrantedAuthority(it) }

    fun getMemberAuthoritiesAsString(): List<String> =
            emptyList()

    fun getProfileImageUrlOrDefaultUrl(): String =
            profileImageUrl?.takeIf { it.isNotBlank() } ?: "/default-profile.png"
}