package com.project2.domain.post.service

import com.project2.domain.member.entity.Member
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.post.dto.comment.CommentRequestDTO
import com.project2.domain.post.dto.comment.CommentResponseDTO
import com.project2.domain.post.entity.Comment
import com.project2.domain.post.entity.Post
import com.project2.domain.post.mapper.CommentMapper
import com.project2.domain.post.repository.CommentRepository
import com.project2.domain.post.repository.PostRepository
import com.project2.global.dto.Empty
import com.project2.global.dto.RsData
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
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
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
        testPost = Post().apply { id = 1L }
        testUser = Member.builder().id(1L).email("test@example.com").nickname("TestUser").build()

        parentComment = Comment().apply {
            id = 101L
            content = "부모 댓글"
            post = testPost
            member = testUser
            depth = 0
        }

        childComment = Comment().apply {
            id = 102L
            content = "대댓글"
            post = testPost
            member = testUser
            parent = parentComment
            depth = 1
        }

        requestDTO = CommentRequestDTO("Updated Comment", null)
    }

    @Test
    @DisplayName("댓글 작성 성공 - 부모 댓글")
    fun `create parent comment success`() {
        `when`(rq.getActor()).thenReturn(testUser)
        `when`(memberRepository.findById(testUser.id!!)).thenReturn(Optional.of(testUser))
        `when`(postRepository.findById(testPost.id!!)).thenReturn(Optional.of(testPost))
        `when`(commentMapper.toEntity(any(), any(), any(), isNull())).thenReturn(parentComment)
        `when`(commentRepository.save(any())).thenReturn(parentComment)
        `when`(commentMapper.toResponseDTO(any(), anyString()))
            .thenReturn(CommentResponseDTO(101L, "부모 댓글", "TestUser", null))

        val response = commentService.createComment(testPost.id!!, requestDTO)

        assertThat(response.code).isEqualTo("200")
        assertThat(response.data).isNotNull
        assertThat(response.data!!.content).isEqualTo("부모 댓글")
    }

    @Test
    @DisplayName("댓글 작성 성공 - 대댓글")
    fun `create child comment success`() {
        `when`(rq.getActor()).thenReturn(testUser)
        `when`(memberRepository.findById(testUser.id!!)).thenReturn(Optional.of(testUser))
        `when`(postRepository.findById(testPost.id!!)).thenReturn(Optional.of(testPost))
        `when`(commentRepository.findById(parentComment.id!!)).thenReturn(Optional.of(parentComment))
        `when`(commentMapper.toEntity(any(), any(), any(), any())).thenReturn(childComment)
        `when`(commentRepository.save(any())).thenReturn(childComment)
        `when`(commentMapper.toResponseDTO(any(), anyString()))
            .thenReturn(CommentResponseDTO(102L, "대댓글", "TestUser", 101L))

        val requestWithParent = CommentRequestDTO("대댓글", parentComment.id)
        val response = commentService.createComment(testPost.id!!, requestWithParent)

        assertThat(response.code).isEqualTo("200")
        assertThat(response.data!!.parentId).isEqualTo(parentComment.id)
    }

    @Test
    @DisplayName("댓글 수정 성공")
    fun `update comment success`() {
        `when`(rq.getActor()).thenReturn(testUser)
        `when`(memberRepository.findById(testUser.id!!)).thenReturn(Optional.of(testUser))
        `when`(commentRepository.findById(parentComment.id!!)).thenReturn(Optional.of(parentComment))
        `when`(commentMapper.toResponseDTO(any(), anyString()))
            .thenReturn(CommentResponseDTO(101L, "Updated Comment", "TestUser", null))

        val response = commentService.updateComment(parentComment.id!!, requestDTO)

        assertThat(response.code).isEqualTo("200")
        assertThat(parentComment.content).isEqualTo(requestDTO.content)
    }

    @Test
    @DisplayName("댓글 수정 실패 - 권한 없음")
    fun `update comment fail - no permission`() {
        val anotherUser = Member.builder().id(2L).build()
        `when`(rq.getActor()).thenReturn(anotherUser)
        `when`(commentRepository.findById(parentComment.id!!)).thenReturn(Optional.of(parentComment))

        assertThatThrownBy {
            commentService.updateComment(parentComment.id!!, requestDTO)
        }.isInstanceOf(ServiceException::class.java)
            .hasMessage("댓글 수정 권한이 없습니다.")
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    fun `delete comment success`() {
        `when`(rq.getActor()).thenReturn(testUser)
        `when`(commentRepository.findById(parentComment.id!!)).thenReturn(Optional.of(parentComment))

        val response = commentService.deleteComment(parentComment.id!!)

        assertThat(response.code).isEqualTo("200")
        verify(commentRepository, times(1)).delete(parentComment)
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 권한 없음")
    fun `delete comment fail - no permission`() {
        val anotherUser = Member.builder().id(2L).build()
        `when`(rq.getActor()).thenReturn(anotherUser)
        `when`(commentRepository.findById(parentComment.id!!)).thenReturn(Optional.of(parentComment))

        assertThatThrownBy {
            commentService.deleteComment(parentComment.id!!)
        }.isInstanceOf(ServiceException::class.java)
            .hasMessage("댓글 삭제 권한이 없습니다.")

        verify(commentRepository, never()).delete(parentComment)
    }
}