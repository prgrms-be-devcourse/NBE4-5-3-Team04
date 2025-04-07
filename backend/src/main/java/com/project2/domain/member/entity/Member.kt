package com.project2.domain.member.entity

import com.project2.domain.member.enums.Provider
import com.project2.global.entity.BaseTime
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.LocalDateTime

@Entity
class Member() : BaseTime() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true)
    var email: String = ""

    @Column(nullable = false, length = 50)
    var nickname: String = ""

    var profileImageUrl: String? = null

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    var provider: Provider? = null

    companion object {
        @JvmStatic
        fun ofId(id: Long) = Member().apply { this.id = id }

        @JvmStatic
        fun ofIdAndEmail(id: Long, email: String) = Member().apply {
            this.id = id
            this.email = email
        }

        @JvmStatic
        fun ofIdAndEmailAndNickname(id: Long, email: String, nickname: String) = Member().apply {
            this.id = id
            this.email = email
            this.nickname = nickname
        }

        @JvmStatic
        fun ofFull(
            id: Long,
            email: String,
            nickname: String,
            provider: Provider,
            profileImageUrl: String? = null
        ) = Member().apply {
            this.id = id
            this.email = email
            this.nickname = nickname
            this.provider = provider
            this.profileImageUrl = profileImageUrl
        }

        @JvmStatic
        fun ofIdAndEmailAndNicknameAndProvider(
            id: Long,
            email: String,
            nickname: String,
            provider: Provider,
        ) = Member().apply {
            this.id = id
            this.email = email
            this.nickname = nickname
            this.provider = provider
        }

        @JvmStatic
        fun ofEmailAndNicknameAndProvider(
            email: String,
            nickname: String,
            provider: Provider,
            profileImageUrl: String? = null
        ) = Member().apply {
            this.email = email
            this.nickname = nickname
            this.provider = provider
            this.profileImageUrl = profileImageUrl
        }

        @JvmStatic
        fun ofMemberAndCreatedDate(
            id: Long,
            email: String,
            nickname: String,
            provider: Provider,
            createdDate: LocalDateTime
        ) = Member().apply {
            this.id = id
            this.email = email
            this.nickname = nickname
            this.provider = provider
            this.createdDate = createdDate
        }
    }

    fun getAuthorities(): Collection<GrantedAuthority> =
        getMemberAuthoritiesAsString().map { SimpleGrantedAuthority(it) }

    fun getMemberAuthoritiesAsString(): List<String> =
        emptyList()

    fun getProfileImageUrlOrDefaultUrl(): String =
        profileImageUrl?.takeIf { it.isNotBlank() } ?: "/default-profile.png"
}