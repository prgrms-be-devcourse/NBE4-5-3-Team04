package com.project2.domain.post.dto

import com.project2.domain.member.dto.AuthorDTO
import com.project2.domain.place.dto.PlaceDTO
import com.project2.domain.post.entity.Post
import com.project2.global.security.SecurityUser

class PostResponseDTO(
        post: Post,
        actor: SecurityUser
) {
    val id: Long = post.id!!
    val title: String = post.title
    val content: String = post.content
    val placeDTO: PlaceDTO = PlaceDTO(post.place.name, post.place.krCategory)
    val likeCount: Int = post.likes.size
    val isLiked: Boolean = post.likes.any { it.member.id == actor.id }
    val scrapCount: Int = post.scraps.size
    val isScrapped: Boolean = post.scraps.any { it.member.id == actor.id }
    val commentCount: Int = post.comments.size
    val imageUrls: List<String> = post.images.map { it.imageUrl }.sorted()
    val author: AuthorDTO = AuthorDTO(
            post.member.id!!,
            post.member.nickname,
            post.member.profileImageUrl
    )
}