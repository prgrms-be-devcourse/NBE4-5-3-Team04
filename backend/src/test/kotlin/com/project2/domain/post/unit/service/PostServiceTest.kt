package com.project2.domain.post.unit.service

import com.project2.domain.member.entity.Member
import com.project2.domain.member.repository.FollowRepository
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.notification.service.NotificationService
import com.project2.domain.place.entity.Place
import com.project2.domain.place.enums.Category
import com.project2.domain.place.enums.Region
import com.project2.domain.place.repository.PlaceRepository
import com.project2.domain.place.service.PlaceService
import com.project2.domain.post.dto.PostRequestDTO
import com.project2.domain.post.entity.Post
import com.project2.domain.post.repository.PostRepository
import com.project2.domain.post.service.PostImageService
import com.project2.domain.post.service.PostService
import com.project2.global.exception.ServiceException
import com.project2.global.security.Rq
import com.project2.global.security.SecurityUser
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PostServiceTest {

    @MockK
    private lateinit var postRepository: PostRepository

    @MockK
    private lateinit var placeRepository: PlaceRepository

    @MockK
    private lateinit var notificationService: NotificationService

    @MockK
    private lateinit var postImageService: PostImageService

    @MockK
    private lateinit var placeService: PlaceService

    @MockK
    private lateinit var rq: Rq

    @MockK
    private lateinit var followRepository: FollowRepository

    @MockK
    private lateinit var memberRepository: MemberRepository

    @InjectMockKs
    private lateinit var postService: PostService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    /**
     * 게시글 생성 성공 테스트
     * - 새로운 게시글이 성공적으로 생성되는지 확인
     */
    @Test
    fun createPostSuccessfully() {
        // given
        val member = Member()
        member.id = 1L
        val place = Place(
            id = 1L,
            name = "테스트 장소",
            latitude = 37.5665,
            longitude = 126.9780,
            region = Region.SEOUL,
            category = Category.CE7,
            posts = mutableListOf()
        )
        val requestDTO = PostRequestDTO()
        requestDTO.title = "테스트 제목"
        requestDTO.content = "테스트 내용"
        requestDTO.placeId = 1L
        requestDTO.placeName = "테스트 장소"
        requestDTO.latitude = 37.5665
        requestDTO.longitude = 126.9780
        requestDTO.region = Region.SEOUL.name
        requestDTO.category = Category.CE7.name

        every { rq.getActor() } returns member
        every { memberRepository.findById(1L) } returns Optional.of(member)
        every { placeRepository.findById(any()) } returns Optional.of(place)
        every { postRepository.save(any()) } returns Post().apply { id = 1L }

        // when
        val result = postService.createPost(requestDTO)

        // then
        assertNotNull(result)
        verify { postRepository.save(any()) }
    }

    /**
     * 게시글 조회 성공 테스트
     * - ID로 게시글을 정상적으로 조회할 수 있는지 확인
     */
    @Test
    fun getPostByIdSuccessfully() {
        // given
        val postId = 1L
        val post = Post().apply { id = postId }
        every { postRepository.findById(postId) } returns Optional.of(post)

        // when
        val result = postService.getPostById(postId)

        // then
        assertNotNull(result)
        assertEquals(postId, result.id)
    }

    /**
     * 존재하지 않는 게시글 조회 시 예외 발생 테스트
     * - 존재하지 않는 게시글 ID로 조회 시 IllegalArgumentException 이 발생하는지 확인
     */
    @Test
    fun throwExceptionWhenPostNotFound() {
        // given
        val postId = 999L
        every { postRepository.findById(postId) } returns Optional.empty()

        // when & then
        assertThrows<IllegalArgumentException> {
            postService.getPostById(postId)
        }
    }

    /**
     * 게시글 수정 성공 테스트
     * - 권한이 있는 사용자가 게시글을 정상적으로 수정할 수 있는지 확인
     */
    @Test
    fun updatePostSuccessfully() {
        // given
        val postId = 1L
        val memberId = 1L
        val authorities = mutableSetOf(SimpleGrantedAuthority("ROLE_USER"))
        val securityUser = SecurityUser(memberId, "test@test.com", authorities)
        val member = Member().apply { id = memberId }
        val post = Post().apply {
            id = postId
            this.member = member
        }
        val requestDTO = PostRequestDTO().apply {
            title = "수정된 제목"
            content = "수정된 내용"
        }

        every { postRepository.findById(postId) } returns Optional.of(post)
        every { postImageService.updateImages(any(), any()) } just Runs

        // when
        postService.updatePost(securityUser, postId, requestDTO)

        // then
        verify { postRepository.findById(postId) }
        verify { postImageService.updateImages(post, requestDTO.images) }
    }

    /**
     * 권한 없는 사용자의 게시글 수정 시도 시 예외 발생 테스트
     * - 권한이 없는 사용자가 게시글 수정 시도 시 ServiceException 이 발생하는지 확인
     */
    @Test
    fun throwExceptionWhenUnauthorizedUserTriesToUpdatePost() {
        // given
        val postId = 1L
        val memberId = 1L
        val otherMemberId = 2L
        val authorities = mutableSetOf(SimpleGrantedAuthority("ROLE_USER"))
        val securityUser = SecurityUser(otherMemberId, "test@test.com", authorities)
        val member = Member().apply { id = memberId }
        val post = Post().apply {
            id = postId
            this.member = member
        }
        val requestDTO = PostRequestDTO()

        every { postRepository.findById(postId) } returns Optional.of(post)

        // when & then
        val exception = assertThrows<ServiceException> {
            postService.updatePost(securityUser, postId, requestDTO)
        }
        assertEquals(HttpStatus.FORBIDDEN.value().toString(), exception.code)
    }

    /**
     * 게시글 삭제 성공 테스트
     * - 권한이 있는 사용자가 게시글을 정상적으로 삭제할 수 있는지 확인
     */
    @Test
    fun deletePostSuccessfully() {
        // given
        val postId = 1L
        val memberId = 1L
        val authorities = mutableSetOf(SimpleGrantedAuthority("ROLE_USER"))
        val securityUser = SecurityUser(memberId, "test@test.com", authorities)
        val member = Member().apply { id = memberId }
        val post = Post().apply {
            id = postId
            this.member = member
        }

        every { postRepository.findById(postId) } returns Optional.of(post)
        every { postRepository.deleteById(postId) } just Runs

        // when
        postService.deletePost(securityUser, postId)

        // then
        verify { postRepository.deleteById(postId) }
    }

    /**
     * 전체 게시글 조회 성공 테스트
     * - 페이징과 필터링이 적용된 게시글 목록을 정상적으로 조회할 수 있는지 확인
     */
    @Test
    fun getAllPostsSuccessfully() {
        // given
        val pageable = PageRequest.of(0, 10)
        val posts = listOf(
                Post().apply { id = 1L },
                Post().apply { id = 2L }
        )
        val pagedPosts = PageImpl(posts)

        every { postRepository.findAll(any(), pageable) } returns pagedPosts

        // when
        val result = postService.getPosts("테스트 장소", Category.CE7, Region.SEOUL, pageable)

        // then
        assertNotNull(result)
        assertEquals(2, result.content.size)
    }
}
