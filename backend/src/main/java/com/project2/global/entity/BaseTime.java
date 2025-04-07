package com.project2.global.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

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
	@Setter(AccessLevel.PRIVATE)
	@JsonProperty("modifiedAt")
	private LocalDateTime modifiedDate;
}