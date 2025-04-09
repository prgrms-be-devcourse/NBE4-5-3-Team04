package com.project2.domain.member.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project2.domain.member.dto.FollowRequestDto;
import com.project2.domain.member.dto.FollowResponseDto;
import com.project2.domain.member.dto.FollowerResponseDto;
import com.project2.domain.member.service.FollowService;
import com.project2.domain.member.service.FollowerService;
import com.project2.domain.member.service.FollowingService;
import com.project2.domain.post.entity.Post;
import com.project2.domain.post.service.PostService;
import com.project2.global.dto.RsData;
import com.project2.global.exception.ServiceException;
import com.project2.global.security.SecurityUser;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

	private final FollowService followService;
	private final FollowerService followerService;
	private final FollowingService followingService;
	private final PostService postService;

	@PostMapping("/{memberid}/follows")
	public RsData<FollowResponseDto> toggleFollow(
		@PathVariable Long memberid,
		@RequestBody FollowRequestDto requestDto
	) {
		requestDto.setFollowerId(memberid);
		return followService.toggleFollow(requestDto);
	}

	@GetMapping("/{memberId}/followers")
	public ResponseEntity<RsData<Page<FollowerResponseDto>>> getFollowers(
			@PathVariable Long memberId,
			@PageableDefault(size = 8) Pageable pageable
	) {
//		try {
			Page<FollowerResponseDto> followers = followerService.getFollowers(memberId, pageable);

			if (followers.isEmpty()) {
				return ResponseEntity.noContent().build();

			}

			System.out.println("가져온 팔로워 페이지 수: " + followers.getTotalPages());
		System.out.println("가져온 팔로워 명 수: " + followers.getTotalElements());

			return ResponseEntity.ok(
					new RsData<>(
							"200",
							"팔로워 목록이 성공적으로 조회되었습니다.",
							followers
					)
			);
//		} catch (ServiceException e) {
//			return ResponseEntity.noContent().build();
//		}
	}
	@GetMapping("/{memberId}/followings")
	public ResponseEntity<RsData<List<FollowerResponseDto>>> getFollowings(@PathVariable Long memberId) {
		try {
			List<FollowerResponseDto> followings = followingService.getFollowings(memberId);

			// Check if the list of followings is empty
			if (followings.isEmpty()) {
				return ResponseEntity.noContent().build();

			}

			return ResponseEntity.ok(
				new RsData<>(
					"200",
					"팔로잉 목록이 성공적으로 조회되었습니다.",
					followings
				)
			);
		} catch (ServiceException e) {
			return ResponseEntity.noContent().build();
		}
	}

	@GetMapping("/{memberId}/following-posts")
	public Page<Post> getFollowingPosts(
		@AuthenticationPrincipal SecurityUser actor,
		@PathVariable Long memberId,
		Pageable pageable
	) {
		// memberId를 사용하여 PostService의 메서드를 호출

		return postService.getFollowingPosts(actor, pageable);

	}
}