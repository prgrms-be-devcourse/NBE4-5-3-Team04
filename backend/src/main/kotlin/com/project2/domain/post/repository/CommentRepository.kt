package com.project2.domain.post.repository

import com.project2.domain.post.dto.comment.ListCommentResponseDTO
import com.project2.domain.post.entity.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository : JpaRepository<Comment, Long> {

	@Query(
		"""
        SELECT new com.project2.domain.post.dto.comment.ListCommentResponseDTO(
            c.id, c.content, c.member.nickname, c.parent.id
        )
        FROM Comment c
        WHERE c.post.id = :postId
        ORDER BY COALESCE(c.parent.id, c.id), c.id
        """
	)
	fun findByPostIdWithParentId(@Param("postId") postId: Long): List<ListCommentResponseDTO>
}