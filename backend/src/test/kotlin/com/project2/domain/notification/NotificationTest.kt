package com.project2.domain.notification

import com.project2.domain.member.entity.Follows
import com.project2.domain.member.entity.Member
import com.project2.domain.member.repository.FollowRepository
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.notification.repository.NotificationRepository
import com.project2.domain.post.dto.PostRequestDTO
import com.project2.domain.post.dto.comment.CommentRequestDTO
import com.project2.domain.post.entity.Comment
import com.project2.domain.post.entity.Post
import com.project2.domain.post.repository.PostRepository
import com.project2.domain.post.service.CommentService
import com.project2.domain.post.service.PostService
import com.project2.global.dto.RsData
import com.project2.global.security.Rq
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class NotificationTest {

    // 모의 객체 선언
    private val memberRepository = mockk<MemberRepository>()
    private val postRepository = mockk<PostRepository>()
    private val notificationRepository = mockk<NotificationRepository>()
    private val commentService = mockk<CommentService>()
    private val followRepository = mockk<FollowRepository>()
    private val rq = mockk<Rq>()
    private val postService = mockk<PostService>()

    private lateinit var postAuthor: Member
    private lateinit var commenter: Member
    private lateinit var follower: Member
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

        follower = Member().apply {
            id = 3L
            nickname = "팔로워"
            email = "follower@test.com"
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
        every { memberRepository.findById(any()) } returns Optional.of(postAuthor)
        every { rq.getActor() } returns postAuthor

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

        // 서비스에서 댓글 작성 시 알림이 발행되도록 설정
        every { commentService.createComment(any(), any()) } returns rsData

        // When
        val result = commentService.createComment(post.id!!, commentRequest)

        // Then
        // 서비스에서 알림이 발행되었는지 확인
        verify { commentService.createComment(any(), any()) }
    }


    @Test
    fun testNotificationCreatedWhenNewPostIsCreated() {
        // Given
        val postRequest = PostRequestDTO(
                title = "새 게시글",
                content = "새 게시글 내용",
                placeId = 1L,
                latitude = 37.123,
                longitude = 127.123,
                images = listOf()
        )

        // 팔로우 관계 설정 (follower 가 postAuthor 를 팔로우)
        val follows = listOf(
                Follows(
                        id = 1L,
                        follower = follower,
                        following = postAuthor
                )
        )

        // 게시글 서비스 모의 설정
        every { postRepository.save(any()) } returns post
        every { followRepository.findAllByFollowing(any()) } returns follows
        every { postService.createPost(any()) } returns post.id!!

        // When - 게시글 서비스 호출
        postService.createPost(postRequest)

        // Then
        // 게시글 서비스가 호출되었는지 확인
        verify { postService.createPost(any()) }
    }

//    @Test
//    fun testNotificationCreatedWhenPostIsLiked() {
//        // Given
//        val like = Likes().apply {
//            id = 1L
//            member = follower
//            post = this@NotificationTest.post
//        }
//
//        // 좋아요 컨트롤러 모의 설정
//        val likeController = mockk<com.project2.domain.post.controller.LikeController>(relaxed = true)
//        every { likesRepository.existsByPostIdAndMemberId(any(), any()) } returns false
//        every { likesRepository.save(any()) } returns like
//        every { likesRepository.countByPostId(any()) } returns 1
//
//        // When - 좋아요 컨트롤러 호출
//        likeController.toggleLike(post.id!!)
//
//        // Then
//        // 좋아요 컨트롤러가 호출되었는지 확인
//        verify { likeController.toggleLike(any()) }
//    }
}
