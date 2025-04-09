package com.project2.domain.post.repository

import com.project2.domain.post.entity.PostImage
import org.springframework.data.jpa.repository.JpaRepository

interface PostImageRepository : JpaRepository<PostImage, Long> {
}