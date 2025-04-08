package com.project2.domain.post.dto.comment

class CommentResponseDTO(
	val id: Long?,
	val content: String,
	val nickname: String,
	val parentId: Long?
)