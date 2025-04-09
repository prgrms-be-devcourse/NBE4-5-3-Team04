package com.project2.global.security

import com.project2.domain.member.repository.MemberRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailService(
        private val memberRepository: MemberRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val member = memberRepository.findByEmail(email)
                .orElseThrow { UsernameNotFoundException("사용자를 찾을 수 없습니다.") }

        return SecurityUser(member.id!!, member.email, member.getAuthorities())
    }
}
