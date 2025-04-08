package com.project2.domain.post.service

import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.post.dto.comment.CommentRequestDTO
import com.project2.domain.post.dto.comment.CommentResponseDTO
import com.project2.domain.post.dto.comment.ListCommentResponseDTO
import com.project2.domain.post.mapper.CommentMapper
import com.project2.domain.post.repository.CommentRepository
import com.project2.domain.post.repository.PostRepository
import com.project2.global.dto.Empty
import com.project2.global.dto.RsData
import com.project2.global.exception.ServiceException
import com.project2.global.security.Rq
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
	private val commentRepository: CommentRepository,
	private val memberRepository: MemberRepository,
	private val postRepository: PostRepository,
	private val commentMapper: CommentMapper,
	private val rq: Rq
) {

	@Transactional
	fun createComment(postId: Long, request: CommentRequestDTO): RsData<CommentResponseDTO> {
		val actor = rq.getActor()
		val member = memberRepository.findById(actor.id!!)
			.orElseThrow { ServiceException("404", "회원 정보를 찾을 수 없습니다.") }

		val post = postRepository.findById(postId)
			.orElseThrow { ServiceException("404", "존재하지 않는 게시글입니다.") }

		val parentComment = request.parentId?.let {
			commentRepository.findById(it).orElse(null)
		}

		if ((parentComment?.depth ?: 0) >= 1) {
			throw ServiceException("400", "대대댓글은 허용되지 않습니다.")
		}

		val comment = commentRepository.save(commentMapper.toEntity(request, post, member, parentComment))
		val responseDTO = commentMapper.toResponseDTO(comment, member.nickname)

		return RsData("200", "댓글이 성공적으로 작성되었습니다.", responseDTO)
	}

	@Transactional(readOnly = true)
	fun getComments(postId: Long): RsData<List<ListCommentResponseDTO>> {
		val flatComments = commentRepository.findByPostIdWithParentId(postId)

		val childComments = flatComments
			.filter { it.parentId != null }
			.groupBy { it.parentId }

		val rootComments = flatComments
			.onEach { it.children.addAll(childComments[it.id] ?: emptyList()) }
			.filter { it.parentId == null }

		return RsData("200", "댓글 목록 조회 성공", rootComments)
	}

	@Transactional
	fun updateComment(commentId: Long, request: CommentRequestDTO): RsData<CommentResponseDTO> {
		val actor = rq.getActor()
		val comment = commentRepository.findById(commentId)
			.orElseThrow { ServiceException("404", "댓글을 찾을 수 없습니다.") }

		if (comment.member.id != actor.id) {
			throw ServiceException("403", "댓글 수정 권한이 없습니다.")
		}

		comment.updateContent(request.content)

		val nickname = memberRepository.findById(actor.id!!)
			.map { it.nickname }
			.orElseThrow { ServiceException("404", "회원 정보를 찾을 수 없습니다.") }

		val responseDTO = commentMapper.toResponseDTO(comment, nickname)
		return RsData("200", "댓글이 성공적으로 수정되었습니다.", responseDTO)
	}

	@Transactional
	fun deleteComment(commentId: Long): RsData<Empty> {
		val actor = rq.getActor()
		val comment = commentRepository.findById(commentId)
			.orElseThrow { ServiceException("404", "존재하지 않는 댓글입니다.") }

		if (comment.member.id != actor.id) {
			throw ServiceException("403", "댓글 삭제 권한이 없습니다.")
		}

		commentRepository.delete(comment)
		return RsData("200", "댓글이 성공적으로 삭제되었습니다.")
	}
}
