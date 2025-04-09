package com.project2.domain.member.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.project2.domain.member.entity.Member
import java.time.format.DateTimeFormatter

data class MemberProfileResponseDTO(
        val nickname: String,
        @JsonProperty(required = true)
        val profileImageUrl: String,
        val totalPostCount: Long,
        val totalFlowerCount: Long,
        val totalFlowingCount: Long,
        val createdMonthYear: String,
        val isMe: Boolean
) {
    companion object {
        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월")

        fun from(
            member: Member,
            totalPostCount: Long,
            totalFlowerCount: Long,
            totalFlowingCount: Long,
            isMe: Boolean
        ): MemberProfileResponseDTO {
            return MemberProfileResponseDTO(
                    nickname = member.nickname,
                    profileImageUrl = member.getProfileImageUrlOrDefaultUrl(),
                    totalPostCount = totalPostCount,
                    totalFlowerCount = totalFlowerCount,
                    totalFlowingCount = totalFlowingCount,
                    createdMonthYear = member.createdDate.format(DATE_FORMATTER),
                    isMe = isMe
            )
        }
    }
}