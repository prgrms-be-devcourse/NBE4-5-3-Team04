package com.project2.domain.post.service

import com.project2.domain.member.entity.Member
import com.project2.domain.place.enums.Category
import com.project2.domain.place.enums.Region
import com.project2.domain.place.repository.PlaceRepository
import com.project2.domain.place.service.PlaceService
import com.project2.domain.post.dto.PostRequestDTO
import com.project2.domain.post.entity.Post
import com.project2.domain.post.repository.PostRepository
import com.project2.domain.post.specification.PostSpecification
import com.project2.global.exception.ServiceException
import com.project2.global.security.Rq
import com.project2.global.security.SecurityUser
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException

@Service
class PostService(
        private val postRepository: PostRepository,
        private val placeRepository: PlaceRepository,
        private val postImageService: PostImageService,
        private val placeService: PlaceService,
        private val rq: Rq
) {
    @Transactional(rollbackFor = [Exception::class])
    @Throws(IOException::class)
    fun createPost(requestDTO: PostRequestDTO): Long {
        val actor = rq.actor

        /* placeId가 존재하는지 먼저 확인한 후, 게시물이 성공적으로 저장되면 장소도 저장 */
        var place = placeRepository.findById(requestDTO.placeId!!).orElse(null)

        if (place == null) {
            place = placeService.savePlace(
                    requestDTO.placeId,
                    requestDTO.placeName,
                    requestDTO.latitude,
                    requestDTO.longitude,
                    requestDTO.region,
                    requestDTO.category
            )
        }

        val post = Post().apply {
            id = null
            content = requestDTO.content
            title = requestDTO.title
            member = actor
            images = mutableSetOf()
            likes = mutableSetOf()
            scraps = mutableSetOf()
            comments = mutableSetOf()
            this.place = place
        }

        val createdPost = postRepository.save(post)

        if (requestDTO.images.isNotEmpty()) {
            postImageService.saveImages(post, requestDTO.images)
        }

        return createdPost.id!!
    }

    // 1. 전체 게시글 조회 (정렬 기준 적용)
    @Transactional(readOnly = true)
    fun getPosts(placeName: String?, category: Category?, region: Region?, pageable: Pageable): Page<Post> {
        // 동적 검색 적용
        val spec = PostSpecification.filterByPlaceAndCategory(placeName, category, region)
        return postRepository.findAll(spec, pageable)
    }

    // 2. 사용자가 좋아요 누른 게시글 조회
    @Transactional(readOnly = true)
    fun getLikedPosts(actor: SecurityUser, pageable: Pageable): Page<Post> {
        return postRepository.findLikedPosts(actor.id, pageable)
    }

    // 3. 사용자가 스크랩한 게시글 조회
    @Transactional(readOnly = true)
    fun getScrappedPosts(actor: SecurityUser, pageable: Pageable): Page<Post> {
        return postRepository.findScrappedPosts(actor.id, pageable)
    }

    // 4. 사용자의 팔로워들의 게시글 조회
    @Transactional(readOnly = true)
    fun getFollowingPosts(actor: SecurityUser, pageable: Pageable): Page<Post> {
        return postRepository.findFollowingPosts(actor.id, pageable)
    }

    // 5. 특정 사용자의 게시글 조회
    @Transactional(readOnly = true)
    fun getPostsByMemberId(targetMemberId: Long, pageable: Pageable): Page<Post> {
        return postRepository.findPostsByMember(targetMemberId, pageable)
    }

    // 6. 특정 장소의 게시글 조회
    @Transactional(readOnly = true)
    fun getPostsByPlaceId(placeId: Long, pageable: Pageable): Page<Post> {
        return postRepository.findPostsByPlace(placeId, pageable)
    }

    @Transactional(readOnly = true)
    fun getPostById(postId: Long): Post {
        return postRepository.findById(postId).orElseThrow { IllegalArgumentException("해당 게시글이 존재하지 않습니다.") }
    }

    @Transactional
    @Throws(IOException::class)
    fun updatePost(actor: SecurityUser, postId: Long, requestDTO: PostRequestDTO) {
        val post = postRepository.findById(postId)
                .orElseThrow { IllegalArgumentException("해당 게시글이 존재하지 않습니다.") }

        if (post.member.id != actor.id) {
            throw ServiceException(HttpStatus.FORBIDDEN.value().toString(), "게시글 수정 권한이 없습니다.")
        }

        post.update(requestDTO.title, requestDTO.content)
        postImageService.updateImages(post, requestDTO.images)
    }

    @Transactional
    fun deletePost(actor: SecurityUser, postId: Long) {
        val post = postRepository.findById(postId)
                .orElseThrow { IllegalArgumentException("해당 게시글이 존재하지 않습니다.") }

        if (post.member.id != actor.id) {
            throw ServiceException(HttpStatus.FORBIDDEN.value().toString(), "게시글 삭제 권한이 없습니다.")
        }
        postRepository.deleteById(postId)
    }

    fun getCountByMember(actor: Member): Long {
        return postRepository.countByMember(actor)
    }

    fun getPostByIdForEdit(postId: Long): Post {
        return postRepository.findByIdForEdit(postId) ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
    }
}
