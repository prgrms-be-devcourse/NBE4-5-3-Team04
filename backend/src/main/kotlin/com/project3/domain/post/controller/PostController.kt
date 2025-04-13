package com.project3.domain.post.controller

import com.project3.domain.place.enums.Category
import com.project3.domain.place.enums.Region
import com.project3.domain.post.dto.PostDetailResponseDTO
import com.project3.domain.post.dto.PostFormResponseDTO
import com.project3.domain.post.dto.PostRequestDTO
import com.project3.domain.post.dto.PostResponseDTO
import com.project3.domain.post.service.PostService
import com.project3.global.dto.RsData
import com.project3.global.security.SecurityUser
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/posts")
class PostController(
        private val postService: PostService
) {

    @PostMapping
    fun createPost(@Valid @ModelAttribute postRequestDTO: PostRequestDTO): RsData<Long> {
        val postId = postService.createPost(postRequestDTO)
        return RsData(HttpStatus.CREATED.value().toString(), "게시글이 성공적으로 생성되었습니다.", postId)
    }

    /**
     * 1. 전체 게시글 조회 (정렬 기준 적용)
     */
    @GetMapping
    fun getPosts(
            @AuthenticationPrincipal actor: SecurityUser,
            @RequestParam(required = false) placeName: String?,
            @RequestParam(required = false) category: Category?,
            @RequestParam(required = false) region: Region?,
            pageable: Pageable
    ): RsData<Page<PostResponseDTO>> {
        val posts = postService.getPosts(placeName, category, region, pageable)
        return RsData(
                HttpStatus.OK.value().toString(),
                "게시글 조회 성공",
                posts.map { post -> PostResponseDTO(post, actor) }
        )
    }

    /**
     * 2. 사용자가 좋아요 누른 게시글 조회
     */
    @GetMapping("/liked")
    fun getLikedPosts(
            @AuthenticationPrincipal actor: SecurityUser,
            pageable: Pageable
    ): RsData<Page<PostResponseDTO>> {
        val posts = postService.getLikedPosts(actor, pageable)
        return RsData(
                HttpStatus.OK.value().toString(),
                "게시글 조회 성공",
                posts.map { post -> PostResponseDTO(post, actor) }
        )
    }

    /**
     * 3. 사용자가 스크랩한 게시글 조회
     */
    @GetMapping("/scrapped")
    fun getScrappedPosts(
            @AuthenticationPrincipal actor: SecurityUser,
            pageable: Pageable
    ): RsData<Page<PostResponseDTO>> {
        val posts = postService.getScrappedPosts(actor, pageable)
        return RsData(
                HttpStatus.OK.value().toString(),
                "게시글 조회 성공",
                posts.map { post -> PostResponseDTO(post, actor) }
        )
    }

    /**
     * 4. 사용자의 팔로워들의 게시글 조회
     */
    @GetMapping("/following")
    fun getFollowingPosts(
            @AuthenticationPrincipal actor: SecurityUser,
            pageable: Pageable
    ): RsData<Page<PostResponseDTO>> {
        val posts = postService.getFollowingPosts(actor, pageable)
        return RsData(
                HttpStatus.OK.value().toString(),
                "게시글 조회 성공",
                posts.map { post -> PostResponseDTO(post, actor) }
        )
    }

    /**
     * 5. 특정 사용자의 게시글 조회
     */
    @GetMapping("/member/{memberId}")
    fun getPostsByMember(
            @AuthenticationPrincipal actor: SecurityUser,
            @PathVariable("memberId") memberId: Long,
            pageable: Pageable
    ): RsData<Page<PostResponseDTO>> {
        val posts = postService.getPostsByMemberId(memberId, pageable)
        return RsData(
                HttpStatus.OK.value().toString(),
                "게시글 조회 성공",
                posts.map { post -> PostResponseDTO(post, actor) }
        )
    }

    /**
     * 6. 특정 장소의 게시글 조회
     */
    @GetMapping("/place/{placeId}")
    fun getPostsByPlace(
            @AuthenticationPrincipal actor: SecurityUser,
            @PathVariable("placeId") placeId: Long,
            pageable: Pageable
    ): RsData<Page<PostResponseDTO>> {
        val posts = postService.getPostsByPlaceId(placeId, pageable)
        return RsData(
                HttpStatus.OK.value().toString(),
                "게시글 조회 성공",
                posts.map { post -> PostResponseDTO(post, actor) }
        )
    }

    @GetMapping("/{postId}")
    fun getPostById(
            @PathVariable postId: Long,
            @AuthenticationPrincipal actor: SecurityUser
    ): RsData<PostDetailResponseDTO> {
        val post = postService.getPostById(postId)
        return RsData(
                HttpStatus.OK.value().toString(),
                "게시글 조회 성공",
                PostDetailResponseDTO(post, actor)
        )
    }

    @GetMapping("/{postId}/for-edit")
    fun getPostByIdForEdit(
            @PathVariable postId: Long,
            @AuthenticationPrincipal actor: SecurityUser
    ): RsData<PostFormResponseDTO> {
        val post = postService.getPostByIdForEdit(postId)
        if (post.member.id != actor.id) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "유효하지 않은 사용자")
        }
        return RsData(
                HttpStatus.OK.value().toString(),
                "게시글 조회 성공",
                PostFormResponseDTO(post)
        )
    }

    @PutMapping("/{postId}")
    fun updatePost(
            @AuthenticationPrincipal actor: SecurityUser,
            @PathVariable postId: Long,
            @Valid @ModelAttribute postRequestDTO: PostRequestDTO
    ): RsData<Long> {
        postService.updatePost(actor, postId, postRequestDTO)
        return RsData(
                HttpStatus.OK.value().toString(),
                "게시글이 성공적으로 수정되었습니다.",
                postId
        )
    }

    @DeleteMapping("/{postId}")
    fun deletePost(
            @AuthenticationPrincipal actor: SecurityUser,
            @PathVariable postId: Long
    ): RsData<Void> {
        postService.deletePost(actor, postId)
        return RsData(
                HttpStatus.OK.value().toString(),
                "게시글이 성공적으로 삭제되었습니다."
        )
    }
}
