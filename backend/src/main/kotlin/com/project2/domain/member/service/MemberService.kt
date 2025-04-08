package com.project2.domain.member.service

import com.project2.domain.member.entity.Member
import com.project2.domain.member.enums.Provider
import com.project2.domain.member.repository.MemberRepository
import com.project2.global.exception.ServiceException
import com.project2.global.util.ImageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class MemberService(private val memberRepository: MemberRepository, private val imageService: ImageService) {

    @Transactional
    fun signUp(email: String, nickname: String, profileImage: String, provider: Provider): Member {
        val member = memberRepository.save(Member.builder().email(email).nickname(nickname).provider(provider).profileImageUrl("").build())

        val profileImagePath = imageService.downloadProfileImage(profileImage, member.id!!)
        member.profileImageUrl = profileImagePath
        return member
    }

    @Transactional
    fun findById(id: Long): Optional<Member> = memberRepository.findById(id)

    @Transactional(readOnly = true)
    fun findByIdOrThrow(id: Long): Member = memberRepository.findById(id).orElseThrow {
        ServiceException("404", "사용자를 찾을 수 없습니다.")
    }

    fun findByEmail(email: String): Optional<Member> = memberRepository.findByEmail(email)

    fun findAllMembers(): List<Member> = memberRepository.findAll()

    @Transactional
    fun updateNickname(memberId: Long, nickname: String) {
        val member = findByIdOrThrow(memberId)
        member.nickname = nickname
    }

    @Transactional
    fun updateProfileImageUrl(memberId: Long, profileImage: MultipartFile) {
        val member = findByIdOrThrow(memberId)
        val savedImagePath = imageService.storeProfileImage(memberId, profileImage)
        member.profileImageUrl = savedImagePath
    }
}