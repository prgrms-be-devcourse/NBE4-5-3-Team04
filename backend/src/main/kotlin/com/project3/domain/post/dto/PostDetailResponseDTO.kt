package com.project3.domain.post.dto

import com.project3.domain.member.dto.AuthorDTO
import com.project3.domain.place.dto.PlaceDTO
import com.project3.domain.post.entity.Post
import com.project3.global.security.SecurityUser
import java.time.LocalDateTime

class PostDetailResponseDTO(
        post: Post,
        actor: SecurityUser
) {
    val id: Long = post.id!!
    val title: String = post.title
    val content: String = post.content
    val imageUrls: List<String> = post.images.map { it.imageUrl }.sorted()
    val likeCount: Int = post.likes.size
    val scrapCount: Int = post.scraps.size
    val isLiked: Boolean = post.likes.any { it.member.id == actor.id }
    val isScrapped: Boolean = post.scraps.any { it.member.id == actor.id }
    val createdDate: LocalDateTime = post.createdDate
    val modifiedDate: LocalDateTime = post.modifiedDate
    val placeDTO: PlaceDTO = PlaceDTO(post.place.name!!, post.place.krCategory)
    val authorDTO: AuthorDTO = AuthorDTO.from(post.member)
}