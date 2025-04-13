package com.project3.domain.post.controller

import com.project3.domain.post.dto.comment.CommentRequestDTO
import com.project3.domain.post.dto.comment.CommentResponseDTO
import com.project3.domain.post.dto.comment.ListCommentResponseDTO
import com.project3.domain.post.service.CommentService
import com.project3.global.dto.Empty
import com.project3.global.dto.RsData
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class CommentController(
	private val commentService: CommentService
) {

	@PostMapping("/posts/{postId}/comments")
	fun createComment(
		@PathVariable postId: Long,
		@RequestBody request: CommentRequestDTO
	): RsData<CommentResponseDTO> {
		return commentService.createComment(postId, request)
	}

	@GetMapping("/posts/{postId}/comments")
	fun getComments(
		@PathVariable postId: Long
	): RsData<List<ListCommentResponseDTO>> {
		return commentService.getComments(postId)
	}

	@PutMapping("/comments/{commentId}")
	fun updateComment(
		@PathVariable commentId: Long,
		@RequestBody request: CommentRequestDTO
	): RsData<CommentResponseDTO> {
		return commentService.updateComment(commentId, request)
	}

	@DeleteMapping("/comments/{commentId}")
	fun deleteComment(
		@PathVariable commentId: Long
	): RsData<Empty> {
		return commentService.deleteComment(commentId)
	}
}