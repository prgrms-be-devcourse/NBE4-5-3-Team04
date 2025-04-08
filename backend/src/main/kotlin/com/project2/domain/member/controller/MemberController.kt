package com.project2.domain.member.controller

import com.project2.domain.member.dto.MemberDTO
import com.project2.domain.member.dto.MemberProfileResponseDTO
import com.project2.domain.member.dto.UpdateNicknameDTO
import com.project2.domain.member.service.*
import com.project2.domain.post.service.PostService
import com.project2.global.dto.Empty
import com.project2.global.dto.RsData
import com.project2.global.security.Rq
import com.project2.global.security.SecurityUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Tag(name = "MemberController", description = "회원 관련 API")
@RestController
@RequestMapping("/api/members")
class MemberController(
        private val authService: AuthService,
        private val memberService: MemberService,
        private val postService: PostService,
        private val followerService: FollowerService,
        private val followingService: FollowingService,
        private val rq: Rq
) {

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    fun me(): RsData<MemberDTO> {
        val actor = rq.actor
        val realActor = rq.getRealActor(actor)
        return RsData("200", "내 정보 조회가 완료되었습니다.", MemberDTO.from(realActor))
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃 처리")
    @DeleteMapping("/logout")
    fun logout(): RsData<Empty> {
        return RsData("200", "로그아웃이 완료되었습니다.", Empty())
    }

    @Operation(summary = "리프레시 토큰을 이용한 액세스 토큰 갱신")
    @GetMapping("/refresh")
    fun refreshAccessToken(): RsData<MemberDTO> {
        val refreshToken = rq.getValueFromCookie("refreshToken")
                ?: return RsData("401", "리프레시 토큰이 제공되지 않았습니다.")

        val actor = authService.getMemberByRefreshTokenOrThrow(refreshToken)
        val realActor = rq.getRealActor(actor)
        val newAccessToken = authService.genAccessToken(realActor)
        rq.addCookie("accessToken", newAccessToken, false)

        return RsData("200", "액세스 토큰이 갱신되었습니다.", MemberDTO.from(realActor))
    }

    @Operation(summary = "사용자 정보 프로필을 조회합니다.")
    @GetMapping("/{memberId}")
    fun getMemberProfile(
            @AuthenticationPrincipal actor: SecurityUser,
            @PathVariable memberId: Long
    ): RsData<MemberProfileResponseDTO> {
        val member = memberService.findByIdOrThrow(memberId)
        val isMe = actor.id == member.id
        val totalPostCount = postService.getCountByMember(member)
        val totalFollowersCount = followerService.getFollowersCount(member)
        val totalFollowingsCount = followingService.getFollowingsCount(member)

        return RsData(
                "200", "사용자 프로필 조회가 완료되었습니다.",
                MemberProfileResponseDTO.from(member, totalPostCount, totalFollowersCount, totalFollowingsCount, isMe)
        )
    }

    @Operation(summary = "전체 회원 조회")
    @GetMapping("/totalMember")
    fun getAllMembers(): RsData<List<MemberDTO>> {
        val members = memberService.findAllMembers()
        val memberDTOs = members.map { MemberDTO.from(it) }
        return RsData("200", "전체 회원 조회가 완료되었습니다.", memberDTOs)
    }

    @Operation(summary = "사용자 정보 프로필 이미지를 수정합니다.")
    @PutMapping("/profile-image/{memberId}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateProfileImage(
            @AuthenticationPrincipal actor: SecurityUser,
            @PathVariable memberId: Long,
            @RequestParam("profileImage") profileImage: MultipartFile
    ): RsData<MemberProfileResponseDTO> {
        authService.validateOwner(actor.id, memberId, "본인의 이미지만 수정이 가능합니다.")
        memberService.updateProfileImageUrl(memberId, profileImage)
        return getMemberProfile(actor, memberId)
    }

    @Operation(summary = "사용자 닉네임을 수정합니다.")
    @PutMapping("/nickname/{memberId}")
    fun updateNickname(
            @AuthenticationPrincipal actor: SecurityUser,
            @PathVariable memberId: Long,
            @RequestBody updateNicknameDTO: UpdateNicknameDTO
    ): RsData<MemberProfileResponseDTO> {
        authService.validateOwner(actor.id, memberId, "본인의 닉네임만 수정이 가능합니다.")
        memberService.updateNickname(memberId, updateNicknameDTO.nickname)
        return getMemberProfile(actor, memberId)
    }
}