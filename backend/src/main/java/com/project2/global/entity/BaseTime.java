package com.project2.global.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@Getter
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
public class BaseTime {

	@CreatedDate
	@Setter(AccessLevel.PROTECTED)
	@JsonProperty("createdAt")
	public LocalDateTime createdDate; // Java에서 Kotlin변환으로 인한 Builder 미사용으로 접근자(public) 임시 변경

	@LastModifiedDate
	@Setter(AccessLevel.PUBLIC)
	@JsonProperty("modifiedAt")
	public LocalDateTime modifiedDate;
}