package com.project2.domain.member.controller;

import com.project2.domain.member.dto.MemberDTO;
import com.project2.domain.member.dto.MemberProfileResponseDTO;
import com.project2.domain.member.dto.UpdateNicknameDTO;
import com.project2.domain.member.entity.Member;
import com.project2.domain.member.service.AuthService;
import com.project2.domain.member.service.FollowerService;
import com.project2.domain.member.service.FollowingService;
import com.project2.domain.member.service.MemberService;
import com.project2.domain.post.service.PostService;
import com.project2.global.dto.Empty;
import com.project2.global.dto.RsData;
import com.project2.global.security.Rq;
import com.project2.global.security.SecurityUser;
import com.project2.global.util.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "MemberController", description = "회원 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final AuthService authService;
    private final MemberService memberService;
    private final PostService postService;
    private final FollowerService followerService;
    private final FollowingService followingService;
    private final ImageService imageService;
    private final Rq rq;

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public RsData<MemberDTO> me() {

        Member actor = rq.getActor();
        Member realActor = rq.getRealActor(actor);

        return new RsData<>("200", "내 정보 조회가 완료되었습니다.", MemberDTO.Companion.from(realActor));
    }

    /**
     * 로그아웃 API 엔드포인트
     * 실제 로그아웃 처리는 Spring Security의 LogoutFilter에서 담당합니다.
     * 이 메서드는 SpringDoc을 통해 프론트엔드에 API 명세를 제공하기 위한 용도입니다.
     *
     * @return 로그아웃 성공 응답
     */
    @Operation(summary = "로그아웃", description = "사용자 로그아웃 처리")
    @DeleteMapping("/logout")
    public RsData<Empty> logout() {
        return new RsData<>("200", "로그아웃이 완료되었습니다.", new Empty());
    }

    @Operation(summary = "리프레시 토큰을 이용한 액세스 토큰 갱신")
    @GetMapping("/refresh")
    public RsData<MemberDTO> refreshAccessToken() {
        String refreshToken = rq.getValueFromCookie("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return new RsData<>("401", "리프레시 토큰이 제공되지 않았습니다.");
        }

        Member actor = authService.getMemberByRefreshTokenOrThrow(refreshToken);
        Member realActor = rq.getRealActor(actor);
        String newAccessToken = authService.genAccessToken(realActor);
        rq.addCookie("accessToken", newAccessToken, false);

        return new RsData<>("200", "액세스 토큰이 갱신되었습니다.", MemberDTO.Companion.from(realActor));
    }

    @Operation(summary = "사용자 정보 프로필을 조회합니다.")
    @GetMapping("/{memberId}")
    public RsData<MemberProfileResponseDTO> getMemberProfile(
            @AuthenticationPrincipal SecurityUser actor,
            @PathVariable long memberId) {

        Member member = this.memberService.findByIdOrThrow(memberId);
        boolean isMe = actor.getId() == member.getId();
        long totalPostCount = this.postService.getCountByMember(member);
        long totalFollowersCount = this.followerService.getFollowersCount(member);
        long totalFollowingsCount = this.followingService.getFollowingsCount(member);

        return new RsData<>("200", "사용자 프로필 조회가 완료되었습니다.",
                MemberProfileResponseDTO.Companion.from(member, totalPostCount, totalFollowersCount, totalFollowingsCount, isMe));
    }

    @Operation(summary = "전체 회원 조회")
    @GetMapping("/totalMember")
    public RsData<List<MemberDTO>> getAllMembers() {
        List<Member> members = memberService.findAllMembers(); // 모든 회원을 조회하는 메서드 호출
        List<MemberDTO> memberDTOs = members.stream()
                .map(MemberDTO.Companion::from) // Member 객체를 MemberDTO로 변환
                .collect(Collectors.toList());

        return new RsData<>(
                "200",
                "전체 회원 조회가 완료되었습니다.",
                memberDTOs
        );
    }

    @Operation(summary = "사용자 정보 프로필 이미지를 수정합니다.")
    @PutMapping(value = "/profile-image/{memberId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RsData<MemberProfileResponseDTO> updateProfileImage(
            @AuthenticationPrincipal SecurityUser actor,
            @PathVariable long memberId,
            @RequestParam("profileImage") MultipartFile profileImage) {

        authService.validateOwner(actor.getId(), memberId, "본인의 이미지만 수정이 가능합니다.");
        // DB에 저장
        Member updatedMember = memberService.updateProfileImageUrl(memberId, profileImage);

        return getMemberProfile(actor, updatedMember.getId());
    }

    @Operation(summary = "사용자 닉네임을 수정합니다.")
    @PutMapping("/nickname/{memberId}")
    public RsData<MemberProfileResponseDTO> updateNickname(
            @AuthenticationPrincipal SecurityUser actor,
            @PathVariable long memberId,
            @RequestBody UpdateNicknameDTO updateNicknameDTO) {

        authService.validateOwner(actor.getId(), memberId, "본인의 닉네임만 수정이 가능합니다.");
        Member updatedMember = memberService.updateNickname(memberId, updateNicknameDTO.getNickname());
        return getMemberProfile(actor, updatedMember.getId());
    }
}
