package com.project2.domain.member.entity

import com.project2.domain.member.enums.Provider
import com.project2.global.entity.BaseTime
import jakarta.persistence.*
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
        fun builder() = MemberBuilder()
    }

    class MemberBuilder {
        private var id: Long? = null
        private var email: String = ""
        private var nickname: String = ""
        private var profileImageUrl: String? = null
        private var provider: Provider? = null
        private var createdDate: LocalDateTime? = null

        fun id(id: Long?) = apply { this.id = id }
        fun email(email: String) = apply { this.email = email }
        fun nickname(nickname: String) = apply { this.nickname = nickname }
        fun profileImageUrl(profileImageUrl: String?) = apply { this.profileImageUrl = profileImageUrl }
        fun provider(provider: Provider) = apply { this.provider = provider }
        fun createdDate(createdDate: LocalDateTime) = apply { this.createdDate = createdDate }

        fun build() = Member().apply {
            this.id = this@MemberBuilder.id
            this.email = this@MemberBuilder.email
            this.nickname = this@MemberBuilder.nickname
            this.profileImageUrl = this@MemberBuilder.profileImageUrl
            this.provider = this@MemberBuilder.provider
            if (this@MemberBuilder.createdDate != null) {
                this.createdDate = this@MemberBuilder.createdDate!!
            }
        }
    }

    fun getAuthorities(): Collection<GrantedAuthority> =

            getMemberAuthoritiesAsString().map { SimpleGrantedAuthority(it) }

    fun getMemberAuthoritiesAsString(): List<String> =
            emptyList()

    fun getProfileImageUrlOrDefaultUrl(): String =
            profileImageUrl?.takeIf { it.isNotBlank() } ?: "/default-profile.png"

}