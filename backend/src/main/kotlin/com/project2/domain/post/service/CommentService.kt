package com.project2.domain.post.service

import com.project2.domain.member.entity.Member
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.notification.enums.NotificationType
import com.project2.domain.notification.event.NotificationEvent
import com.project2.domain.notification.service.NotificationService
import com.project2.domain.post.dto.comment.CommentRequestDTO
import com.project2.domain.post.dto.comment.CommentResponseDTO
import com.project2.domain.post.dto.comment.ListCommentResponseDTO
import com.project2.domain.post.entity.Comment
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
		private val rq: Rq,
		private val notificationService: NotificationService
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

		// 비동기로 알림 처리
		val event = NotificationEvent(
				receiver = post.member,
				sender = member,
				type = NotificationType.NEW_COMMENT,
				content = "${member.nickname}님이 회원님의 게시글에 댓글을 달았습니다: ${request.content}",
				relatedId = post.id!!  // 댓글 ID 대신 게시글 ID 사용
		)
		// 비동기 처리를 위해 notificationService 사용
		notificationService.processNotificationAsync(event)

		// 대댓글인 경우 추가 알림 생성
		if (parentComment != null) {
			// 대댓글이 달릴 때 원 댓글 작성자에게 알림
			notifyReply(member, parentComment, request.content)
		}

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

	// 대댓글 알림 처리를 위한 별도 메서드
	private fun notifyReply(member: Member, parentComment: Comment, content: String) {
		// 비동기로 알림 처리
		val event = NotificationEvent(
				receiver = parentComment.member,
				sender = member,
				type = NotificationType.NEW_REPLY,
				content = "${member.nickname}님이 댓글에 대댓글을 달았습니다: $content",
				relatedId = parentComment.post.id!!  // 댓글 ID 대신 게시글 ID 사용
		)
		// 비동기 처리를 위해 notificationService 사용
		notificationService.processNotificationAsync(event)
	}
}
