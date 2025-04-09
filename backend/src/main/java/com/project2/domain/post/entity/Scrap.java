package com.project2.domain.post.entity;

import com.project2.domain.member.entity.Member;
import com.project2.global.entity.BaseTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Scrap extends BaseTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(nullable = false)
	public Post post;	// Kotlin 코드(ToggleMapper 등)에서 접근 가능하도록 임시로 public 설정함
						// 추후 Scrap 엔티티를 Kotlin으로 마이그레이션할 때 접근 제어자 재조정 예정

	@ManyToOne
	@JoinColumn(nullable = false)
	public Member member;
}