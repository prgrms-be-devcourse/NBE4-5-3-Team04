package com.project2.domain.post.dto

import com.project2.domain.post.validation.ImageFile
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.web.multipart.MultipartFile

data class PostRequestDTO(
        @field:NotBlank(message = "제목을 입력해주세요.")
        var title: String = "",

        @field:NotBlank(message = "내용을 입력해주세요.")
        var content: String = "",

        @field:NotNull(message = "장소 ID는 필수입니다.")
        var placeId: Long? = null,

        @field:NotNull(message = "위도(latitude)는 필수입니다.")
        var latitude: Double? = null,

        @field:NotNull(message = "경도(longitude)는 필수입니다.")
        var longitude: Double? = null,

        @field:NotBlank(message = "장소 이름은 필수입니다.")
        var placeName: String = "",

        @field:NotBlank(message = "카테고리는 필수입니다.")
        var category: String = "",

        @field:NotBlank(message = "지역(Region)은 필수입니다.")
        var region: String = "",

        @field:NotNull(message = "회원 ID는 필수입니다.")
        var memberId: Long? = null,

        @field:ImageFile
        var images: List<MultipartFile> = emptyList()
)