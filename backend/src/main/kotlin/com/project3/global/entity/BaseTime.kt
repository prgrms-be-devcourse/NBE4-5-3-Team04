package com.project3.global.entity

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import lombok.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseTime {

    @CreatedDate
    @JsonProperty("createdAt")
    lateinit var createdDate: LocalDateTime

    @LastModifiedDate
    @JsonProperty("modifiedAt")
    lateinit var modifiedDate: LocalDateTime
}