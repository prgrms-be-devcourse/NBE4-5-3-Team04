package com.project2.domain.member.dto;

import lombok.Getter;

@Getter
public class AuthorDTO {
	private Long memberId;
	private String nickname;
	private String profileImageUrl;

	public AuthorDTO(Long memberId, String nickname, String profileImageUrl) {
		this.memberId = memberId;
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
	}
}
