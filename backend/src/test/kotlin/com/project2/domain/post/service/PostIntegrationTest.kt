package com.project2.domain.post.service

import com.project2.domain.member.dto.FollowRequestDto
import com.project2.domain.member.entity.Member
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.place.entity.Place
import com.project2.domain.place.enums.Category
import com.project2.domain.place.enums.Region
import com.project2.domain.place.repository.PlaceRepository
import com.project2.domain.post.dto.PostDetailResponseDTO
import com.project2.domain.post.dto.PostRequestDTO
import com.project2.domain.post.dto.comment.CommentRequestDTO
import com.project2.domain.post.entity.Post
import com.project2.domain.post.repository.PostRepository
import com.project2.global.exception.ServiceException
import com.project2.global.security.Rq
import com.project2.global.security.SecurityUser
import io.mockk.every
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Pageable
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostIntegrationTest @Autowired constructor(
    val postService: PostService,
    val postToggleService: PostToggleService,
    val commentService: CommentService,
    val memberRepository: MemberRepository,
    val placeRepository: PlaceRepository,
    val postRepository: PostRepository,
    val rq: Rq
) {

    private lateinit var member: Member
    private lateinit var otherMember: Member
    private lateinit var place: Place

    @BeforeEach
    fun setup() {
        member = memberRepository.save(Member(email = "test@example.com", nickname = "tester"))
        otherMember = memberRepository.save(Member(email = "other@example.com", nickname = "other"))
        place = placeRepository.save(
            Place(
                id = 1L,
                name = "테스트 장소",
                latitude = 37.5,
                longitude = 127.0,
                region = Region.SEOUL,
                category = Category.CE7
            )
        )
        every { rq.getActor() } returns member
    }

    fun Member.toSecurityUser(): SecurityUser =
        SecurityUser(id = this.id!!, email = this.email, authorities = mutableSetOf(SimpleGrantedAuthority("ROLE_USER")))

    fun createPost(): Long {
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

    @Test
    @DisplayName("게시글 작성 후 좋아요/스크랩 수행하고 상세 조회로 상태 확인")
    fun createPostWithLikeScrapAndDetailCheck() {
        val postId = createPost()
        postToggleService.toggleLikes(member.id!!, postId)
        postToggleService.toggleScrap(member.id!!, postId)
        val detail = PostDetailResponseDTO(postRepository.findById(postId).get(), member.toSecurityUser())
        assertTrue(detail.isLiked)
        assertTrue(detail.isScrapped)
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
        assertEquals("400", exception.code)
    }

    @Test
    @DisplayName("다른 사용자가 게시글을 수정하려 할 경우 예외 발생")
    fun unauthorizedUserCannotUpdatePost() {
        val postId = createPost()
        val actor = SecurityUser(otherMember.id!!, otherMember.email, setOf(SimpleGrantedAuthority("ROLE_USER")))
        val exception = assertThrows<ServiceException> {
            postService.updatePost(actor, postId, PostRequestDTO(title = "x", content = "y"))
        }
        assertEquals("403", exception.code)
    }

    @Test
    @DisplayName("좋아요 2번 토글 후 상태가 정상적으로 유지되는지 확인")
    fun toggleLikeTwiceAndVerifyStatus() {
        val postId = createPost()
        postToggleService.toggleLikes(member.id!!, postId)
        postToggleService.toggleLikes(member.id!!, postId)
        val status = postToggleService.toggleLikes(member.id!!, postId).data
        assertTrue(status.liked)
        assertEquals(1, status.likeCount)
    }

    @Test
    @DisplayName("게시글 삭제 시 댓글, 좋아요, 스크랩도 함께 삭제되어야 한다")
    fun deletePostAlsoDeletesRelations() {
        val postId = createPost()
        commentService.createComment(postId, CommentRequestDTO("댓글", null))
        postToggleService.toggleLikes(member.id!!, postId)
        postToggleService.toggleScrap(member.id!!, postId)
        postService.deletePost(member.toSecurityUser(), postId)
        assertTrue(postRepository.findById(postId).isEmpty)
    }

    @Test
    @DisplayName("PostDetailResponseDTO의 값이 정확히 매핑되는지 확인")
    fun verifyPostDetailResponseValues() {
        val postId = createPost()
        val detail = PostDetailResponseDTO(postRepository.findById(postId).get(), member.toSecurityUser())
        assertEquals("제목", detail.title)
        assertEquals("내용", detail.content)
        assertEquals(place.name, detail.placeDTO.placeName)
        assertEquals(place.category.krCategory, detail.placeDTO.category)
    }

    @Test
    @DisplayName("댓글 목록 조회 시 계층 구조가 올바르게 구성되는지 확인")
    fun verifyCommentHierarchyStructure() {
        val postId = createPost()
        val parent = commentService.createComment(postId, CommentRequestDTO("부모", null)).data.id!!
        commentService.createComment(postId, CommentRequestDTO("자식", parent))
        val list = commentService.getComments(postId).data
        assertEquals(1, list.size)
        assertEquals(1, list[0].children.size)
    }

    @Test
    @DisplayName("좋아요한 게시글 목록 조회 시 정확한 결과 반환 여부 확인")
    fun getLikedPostsShouldReturnCorrectPost() {
        val postId = createPost()
        postToggleService.toggleLikes(member.id!!, postId)
        val result = postService.getLikedPosts(member.toSecurityUser(), Pageable.unpaged())
        assertEquals(1, result.totalElements)
    }

    @Test
    @DisplayName("팔로우 후 피드 조회 시 상대방 게시글이 포함되는지 확인")
    fun followUserAndSeeTheirPostInFeed() {
        postToggleService.toggleFollow(FollowRequestDto(member.id!!, otherMember.id!!))
        postRepository.save(Post().apply {
            this.id = 2L
            this.member = otherMember
            this.title = "다른사람 글"
            this.content = "내용"
            this.place = place
        }).id!!
        val result = postService.getFollowingPosts(member.toSecurityUser(), Pageable.unpaged())
        assertEquals(1, result.totalElements)
        assertEquals("다른사람 글", result.content[0].title)
    }
}