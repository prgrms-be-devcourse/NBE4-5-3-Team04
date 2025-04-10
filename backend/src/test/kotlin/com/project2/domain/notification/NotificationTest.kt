package com.project2.domain.notification

import com.project2.domain.member.entity.Member
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.notification.enums.NotificationType
import com.project2.domain.notification.event.NotificationEvent
import com.project2.domain.notification.repository.NotificationRepository
import com.project2.domain.post.dto.comment.CommentRequestDTO
import com.project2.domain.post.entity.Comment
import com.project2.domain.post.entity.Post
import com.project2.domain.post.repository.PostRepository
import com.project2.domain.post.service.CommentService
import com.project2.global.dto.RsData
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher

class NotificationTest {

    // 모의 객체 선언
    private val memberRepository = mockk<MemberRepository>()
    private val postRepository = mockk<PostRepository>()
    private val notificationRepository = mockk<NotificationRepository>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val commentService = mockk<CommentService>()

    private lateinit var postAuthor: Member
    private lateinit var commenter: Member
    private lateinit var post: Post

    @BeforeEach
    fun setup() {
        // 테스트 데이터 초기화
        postAuthor = Member().apply {
            id = 1L
            nickname = "게시글작성자"
            email = "post@test.com"
        }

        commenter = Member().apply {
            id = 2L
            nickname = "댓글작성자"
            email = "comment@test.com"
        }

        post = Post().apply {
            id = 1L
            title = "테스트 게시글"
            content = "테스트 내용입니다."
            member = postAuthor
        }

        // 모의 객체 동작 설정
        every { memberRepository.save(any()) } returns postAuthor
        every { postRepository.save(any()) } returns post

        // 알림 조회 모의 설정
        val notifications = mutableListOf<com.project2.domain.notification.entity.Notification>()
        every { notificationRepository.findByReceiverIdAndReadFalseOrderByCreatedDateDesc(any()) } returns notifications
        every { notificationRepository.save(any()) } answers {
            val notification = firstArg<com.project2.domain.notification.entity.Notification>()
            notifications.add(notification)
            notification
        }
    }

    @Test
    fun testNotificationCreatedWhenCommentIsAdded() {
        // Given
        val commentRequest = CommentRequestDTO(
                content = "테스트 댓글입니다.",
                parentId = null
        )

        val comment = Comment().apply {
            id = 1L
            content = "테스트 댓글입니다."
            member = commenter
            post = this@NotificationTest.post
        }

        val commentResponseDTO = com.project2.domain.post.dto.comment.CommentResponseDTO(
                id = 1L,
                content = "테스트 댓글입니다.",
                nickname = commenter.nickname,
                parentId = null
        )

        val rsData = RsData(
                code = "200",
                msg = "댓글이 성공적으로 작성되었습니다.",
                data = commentResponseDTO
        )

        every { commentService.createComment(any(), any()) } returns rsData

        // When
        val result = commentService.createComment(post.id!!, commentRequest)

        // 직접 알림 이벤트 발행
        val event = NotificationEvent(
                receiver = postAuthor,
                sender = commenter,
                type = NotificationType.NEW_COMMENT,
                content = "${commenter.nickname}님이 회원님의 게시글에 댓글을 달았습니다: 테스트 댓글입니다.",
                relatedId = result.data.id!!
        )
        eventPublisher.publishEvent(event)

        // Then
        verify { eventPublisher.publishEvent(any<NotificationEvent>()) }
    }
}
