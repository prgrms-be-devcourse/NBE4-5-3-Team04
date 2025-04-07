package com.project2.domain.member.dto

import com.project2.domain.member.entity.Member
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

@Getter
//class FollowerResponseDto(id: Long, email: String, nickname: String, profileImageUrl: String, i: Int) {
//    var userId: Long? = null
//    var email: String? = null
//    var nickname: String? = null
//    var profileImageUrl: String? = null
//    var totalPages:Int = 0 // totalPages 추가
//
//
//    companion object {
//        fun fromEntity(member: Member): FollowerResponseDto {
//            return FollowerResponseDto(
//                member.id!!,
//                member.email!!,
//                member.nickname!!,
//                member.profileImageUrl!!,
//                0 // 초기값 설정 (나중에 컨트롤러에서 설정)
//            )
//        }
//    }
//
//}
class FollowerResponseDto(
    var id: Long? = null,
    var email: String? = null,
    var nickname: String? = null,
    var profileImageUrl: String? = null,
    var totalPages: Int = 0 // totalPages 추가
) {
    constructor(member: Member) : this(
        id = member.id!!,
        email = member.email,
        nickname = member.nickname,
        profileImageUrl = member.profileImageUrl!!,
        totalPages = 0
    )


}
