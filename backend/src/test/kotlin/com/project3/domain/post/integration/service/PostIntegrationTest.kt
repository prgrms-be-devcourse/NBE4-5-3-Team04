package com.project3.domain.post.integration.service

import com.project3.domain.member.dto.FollowRequestDto
import com.project3.domain.member.entity.Member
import com.project3.domain.member.repository.MemberRepository
import com.project3.domain.place.entity.Place
import com.project3.domain.place.enums.Category
import com.project3.domain.place.enums.Region
import com.project3.domain.place.repository.PlaceRepository
import com.project3.domain.post.dto.PostDetailResponseDTO
import com.project3.domain.post.dto.PostRequestDTO
import com.project3.domain.post.dto.comment.CommentRequestDTO
import com.project3.domain.post.entity.Post
import com.project3.domain.post.repository.PostRepository
import com.project3.domain.post.service.CommentService
import com.project3.domain.post.service.PostService
import com.project3.domain.post.service.PostToggleService
import com.project3.global.exception.ServiceException
import com.project3.global.security.Rq
import com.project3.global.security.SecurityUser
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Pageable
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(PostIntegrationTest.MockRqConfig::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostIntegrationTest {

    @TestConfiguration
    class MockRqConfig {
        @Bean
        @Primary
        fun mockRq(): Rq = Mockito.mock(Rq::class.java)
    }

    @Autowired
    lateinit var entityManager: EntityManager
    @Autowired lateinit var postService: PostService
    @Autowired lateinit var postToggleService: PostToggleService
    @Autowired lateinit var commentService: CommentService
    @Autowired lateinit var memberRepository: MemberRepository
    @Autowired lateinit var placeRepository: PlaceRepository
    @Autowired lateinit var postRepository: PostRepository
    @Autowired lateinit var rq: Rq

    private lateinit var member: Member
    private lateinit var otherMember: Member
    private lateinit var place: Place

    @BeforeEach
    fun setup() {
        member = memberRepository.save(Member(email = "test@example.com", nickname = "tester"))
        otherMember = memberRepository.save(Member(email = "other@example.com", nickname = "other"))
        place = placeRepository.save(Place(id = 1L, name = "테스트 장소", latitude = 37.5, longitude = 127.0, region = Region.SEOUL, category = Category.CE7))
        whenever(rq.getActor()).thenReturn(member)
    }

    private fun createPost(): Long {
        val request = PostRequestDTO(
            title = "제목",
            content = "내용",
            placeId = place.id,
            placeName = place.name,
            latitude = place.latitude,
            longitude = place.longitude,
            category = place.category.name,
            region = place.region.name,
            memberId = member.id,
            images = emptyList()
        )
        return postService.createPost(request)
    }

    private fun toSecurityUser(member: Member): SecurityUser =
        SecurityUser(member.id!!, member.email, setOf(SimpleGrantedAuthority("ROLE_USER")))

    @Test
    @DisplayName("게시글 작성 후 좋아요/스크랩 수행하고 상세 조회로 상태 확인")
    fun createPostWithLikeScrapAndDetailCheck() {
        val postId = createPost()

        postToggleService.toggleLikes(member.id!!, postId)
        postToggleService.toggleScrap(member.id!!, postId)

        entityManager.flush()
        entityManager.clear()

        val detail = PostDetailResponseDTO(
            postRepository.findById(postId).get(),
            toSecurityUser(member)
        )

        assertThat(detail.isLiked).isTrue()
        assertThat(detail.isScrapped).isTrue()
    }


    @Test
    @DisplayName("대댓글에 댓글 작성 시 예외가 발생해야 한다")
    fun replyToReplyShouldThrowException() {
        val postId = createPost()
        val parent = commentService.createComment(postId, CommentRequestDTO("댓글", null)).data.id
        val child = commentService.createComment(postId, CommentRequestDTO("대댓글", parent)).data.id
        val exception = assertThrows<ServiceException> {
            commentService.createComment(postId, CommentRequestDTO("대대댓글", child))
        }
        assertThat(exception.code).isEqualTo("400")
    }

    @Test
    @DisplayName("다른 사용자가 게시글을 수정하려 할 경우 예외 발생")
    fun unauthorizedUserCannotUpdatePost() {
        val postId = createPost()
        val attacker = toSecurityUser(otherMember)
        val exception = assertThrows<ServiceException> {
            postService.updatePost(attacker, postId, PostRequestDTO(title = "x", content = "y"))
        }
        assertThat(exception.code).isEqualTo("403")
    }

    @Test
    @DisplayName("좋아요 2번 토글 후 상태가 정상적으로 유지되는지 확인")
    fun toggleLikeTwiceAndVerifyStatus() {
        val postId = createPost()
        postToggleService.toggleLikes(member.id!!, postId)
        postToggleService.toggleLikes(member.id!!, postId)
        val status = postToggleService.toggleLikes(member.id!!, postId).data
        assertThat(status.liked).isTrue()
        assertThat(status.likeCount).isEqualTo(1)
    }

    @Test
    @DisplayName("게시글 삭제 시 댓글, 좋아요, 스크랩도 함께 삭제되어야 한다")
    fun deletePostAlsoDeletesRelations() {
        val postId = createPost()
        commentService.createComment(postId, CommentRequestDTO("댓글", null))
        postToggleService.toggleLikes(member.id!!, postId)
        postToggleService.toggleScrap(member.id!!, postId)
        postService.deletePost(toSecurityUser(member), postId)
        assertThat(postRepository.findById(postId)).isEmpty
    }

    @Test
    @DisplayName("PostDetailResponseDTO 값이 정확히 매핑되는지 확인")
    fun verifyPostDetailResponseValues() {
        val postId = createPost()
        val detail = PostDetailResponseDTO(postRepository.findById(postId).get(), toSecurityUser(member))
        assertThat(detail.title).isEqualTo("제목")
        assertThat(detail.content).isEqualTo("내용")
        assertThat(detail.placeDTO.placeName).isEqualTo(place.name)
        assertThat(detail.placeDTO.category).isEqualTo(place.category.krCategory)
    }

    @Test
    @DisplayName("댓글 목록 조회 시 계층 구조가 올바르게 구성되는지 확인")
    fun verifyCommentHierarchyStructure() {
        val postId = createPost()
        val parent = commentService.createComment(postId, CommentRequestDTO("부모", null)).data.id!!
        commentService.createComment(postId, CommentRequestDTO("자식", parent))
        val list = commentService.getComments(postId).data
        assertThat(list).hasSize(1)
        assertThat(list[0].children).hasSize(1)
    }

    @Test
    @DisplayName("좋아요한 게시글 목록 조회 시 정확한 결과 반환 여부 확인")
    fun getLikedPostsShouldReturnCorrectPost() {
        val postId = createPost()
        postToggleService.toggleLikes(member.id!!, postId)
        val result = postService.getLikedPosts(toSecurityUser(member), Pageable.unpaged())
        assertThat(result.totalElements).isEqualTo(1)
    }

    @Test
    @DisplayName("팔로우 후 피드 조회 시 상대방 게시글이 포함되는지 확인")
    fun followUserAndSeeTheirPostInFeed() {
        val savedPlace = placeRepository.save(Place(
            id = 1L,
            name = "팔로우 테스트 장소",
            latitude = 37.5,
            longitude = 127.0,
            region = Region.SEOUL,
            category = Category.CE7
        ))

        postToggleService.toggleFollow(FollowRequestDto(member.id!!, otherMember.id!!))

        postRepository.save(Post().apply {
            this.member = otherMember
            this.title = "다른사람 글"
            this.content = "내용"
            this.place = savedPlace
        })

        val result = postService.getFollowingPosts(toSecurityUser(member), Pageable.unpaged())
        assertThat(result.totalElements).isEqualTo(1)
        assertThat(result.content[0].title).isEqualTo("다른사람 글")
    }
}