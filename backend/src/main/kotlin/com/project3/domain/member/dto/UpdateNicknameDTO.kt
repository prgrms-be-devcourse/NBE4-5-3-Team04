package com.project3.domain.member.dto

import jakarta.validation.constraints.NotBlank

data class UpdateNicknameDTO(
        @field:NotBlank
        val nickname: String = ""
)