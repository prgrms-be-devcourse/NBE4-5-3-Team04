package com.project2.domain.post.unit.service

import com.project2.domain.member.entity.Member
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.notification.service.NotificationService
import com.project2.domain.post.dto.comment.CommentRequestDTO
import com.project2.domain.post.dto.comment.CommentResponseDTO
import com.project2.domain.post.entity.Comment
import com.project2.domain.post.entity.Post
import com.project2.domain.post.mapper.CommentMapper
import com.project2.domain.post.repository.CommentRepository
import com.project2.domain.post.repository.PostRepository
import com.project2.domain.post.service.CommentService
import com.project2.global.exception.ServiceException
import com.project2.global.security.Rq
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.*

@ExtendWith(MockitoExtension::class)
class CommentServiceTest {

    @Mock
    private lateinit var memberRepository: MemberRepository

    @Mock
    private lateinit var commentRepository: CommentRepository

    @Mock
    private lateinit var postRepository: PostRepository

    @Mock
    private lateinit var notificationService: NotificationService

    @Mock
    private lateinit var rq: Rq

    @Mock
    private lateinit var commentMapper: CommentMapper

    @InjectMocks
    private lateinit var commentService: CommentService

    private lateinit var testUser: Member
    private lateinit var testPost: Post
    private lateinit var parentComment: Comment
    private lateinit var childComment: Comment
    private lateinit var requestDTO: CommentRequestDTO

    @BeforeEach
    fun setUp() {
        testUser = Member().apply {
            id = 1L
            email = "test@example.com"
            nickname = "TestUser"
        }

        testPost = Post().apply {
            id = 1L
            title = "title"
            content = "content"
            member = testUser
            place = mock()
        }

        parentComment = Comment().apply {
            id = 101L; content = "부모 댓글"; post = testPost; member = testUser; depth = 0
        }
        childComment = Comment().apply {
            id = 102L; content = "대댓글"; post = testPost; member = testUser; parent = parentComment; depth = 1
        }
        requestDTO = CommentRequestDTO("Updated Comment", null)
    }

    @Test
    @DisplayName("댓글 작성 성공 - 부모 댓글")
    fun `createComment should succeed when creating a top-level comment`() {
        val request = CommentRequestDTO("부모 댓글입니다.", null)
        val saved = parentComment.apply { content = request.content }

        whenever(rq.getActor()).thenReturn(testUser)
        whenever(memberRepository.findById(testUser.id!!)).thenReturn(Optional.of(testUser))
        whenever(postRepository.findById(testPost.id!!)).thenReturn(Optional.of(testPost))
        whenever(commentMapper.toEntity(request, testPost, testUser, null)).thenReturn(saved)
        whenever(commentRepository.save(saved)).thenReturn(saved)
        whenever(commentMapper.toResponseDTO(saved, testUser.nickname))
                .thenReturn(CommentResponseDTO(saved.id, saved.content, testUser.nickname, null))

        val res = commentService.createComment(testPost.id!!, request)

        assertThat(res.code).isEqualTo("200")
        assertThat(res.data.content).isEqualTo("부모 댓글입니다.")
        assertThat(res.data.parentId).isNull()
    }

    @Test
    @DisplayName("댓글 작성 성공 - 대댓글")
    fun `create child comment success`() {
        val request = CommentRequestDTO("대댓글", 101L)
        val saved = childComment

        whenever(rq.getActor()).thenReturn(testUser)
        whenever(memberRepository.findById(testUser.id!!)).thenReturn(Optional.of(testUser))
        whenever(postRepository.findById(testPost.id!!)).thenReturn(Optional.of(testPost))
        whenever(commentRepository.findById(101L)).thenReturn(Optional.of(parentComment))
        whenever(commentMapper.toEntity(request, testPost, testUser, parentComment)).thenReturn(saved)
        whenever(commentRepository.save(saved)).thenReturn(saved)
        whenever(commentMapper.toResponseDTO(saved, testUser.nickname))
                .thenReturn(CommentResponseDTO(saved.id, saved.content, testUser.nickname, 101L))

        val res = commentService.createComment(testPost.id!!, request)

        assertThat(res.code).isEqualTo("200")
        assertThat(res.data.parentId).isEqualTo(101L)
    }

    @Test
    @DisplayName("댓글 수정 성공")
    fun `update comment success`() {
        val commentToUpdate = Comment().apply {
            id = 101L
            content = "원래 댓글"
            post = testPost
            member = testUser
            depth = 0
        }
        val request = CommentRequestDTO("Updated Comment", null)

        whenever(rq.getActor()).thenReturn(testUser)
        whenever(memberRepository.findById(testUser.id!!)).thenReturn(Optional.of(testUser))
        whenever(commentRepository.findById(101L)).thenReturn(Optional.of(commentToUpdate))
        whenever(commentMapper.toResponseDTO(commentToUpdate, testUser.nickname))
                .thenReturn(CommentResponseDTO(101L, "Updated Comment", "TestUser", null))

        val res = commentService.updateComment(101L, request)

        assertThat(res.code).isEqualTo("200")
        assertThat(commentToUpdate.content).isEqualTo("Updated Comment")
    }

    @Test
    @DisplayName("댓글 수정 실패 - 권한 없음")
    fun `update comment fail - no permission`() {
        val otherUser = Member().apply {
            id = 2L
        }
        whenever(rq.getActor()).thenReturn(otherUser)
        whenever(commentRepository.findById(101L)).thenReturn(Optional.of(parentComment))

        assertThatThrownBy { commentService.updateComment(101L, requestDTO) }
                .isInstanceOf(ServiceException::class.java)
                .hasMessage("댓글 수정 권한이 없습니다.")
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    fun `delete comment success`() {
        whenever(rq.getActor()).thenReturn(testUser)
        whenever(commentRepository.findById(101L)).thenReturn(Optional.of(parentComment))

        val res = commentService.deleteComment(101L)

        assertThat(res.code).isEqualTo("200")
        verify(commentRepository).delete(parentComment)
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 권한 없음")
    fun `delete comment fail - no permission`() {
        val otherUser = Member().apply {
            id = 2L
        }
        whenever(rq.getActor()).thenReturn(otherUser)
        whenever(commentRepository.findById(101L)).thenReturn(Optional.of(parentComment))

        assertThatThrownBy { commentService.deleteComment(101L) }
                .isInstanceOf(ServiceException::class.java)
                .hasMessage("댓글 삭제 권한이 없습니다.")
        verify(commentRepository, never()).delete(any())
    }
}