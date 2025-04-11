package com.project2.domain.post.service

import com.project2.domain.member.entity.Member
import com.project2.domain.member.repository.FollowRepository
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.notification.enums.NotificationType
import com.project2.domain.notification.event.NotificationEvent
import com.project2.domain.notification.service.NotificationService
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
        private val rq: Rq,
        private val followRepository: FollowRepository,
        private val notificationService: NotificationService,
        private val memberRepository: MemberRepository
) {
    @Transactional(rollbackFor = [Exception::class])
    @Throws(IOException::class)
    fun createPost(requestDTO: PostRequestDTO): Long {
        val actor = rq.getActor()
        val member = memberRepository.findById(actor.id!!)
                .orElseThrow { ServiceException("404", "회원 정보를 찾을 수 없습니다.") }

        val placeId = requestDTO.placeId!!
        val latitude = requestDTO.latitude!!
        val longitude = requestDTO.longitude!!

        var place = placeRepository.findById(placeId).orElse(null)

        if (place == null) {
            place = placeService.savePlace(
                    placeId,
                    requestDTO.placeName,
                    latitude,
                    longitude,
                    requestDTO.region,
                    requestDTO.category
            )
        }

        val post = Post().apply {
            id = null
            content = requestDTO.content
            title = requestDTO.title
            this.member = member
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

        // 팔로워들에게 새 게시글 알림 전송
        sendNewPostNotifications(member, createdPost)

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

    /**
     * 팔로워들에게 새 게시글 알림 전송
     */
    private fun sendNewPostNotifications(author: Member, post: Post) {
        try {
            // 작성자 정보 확인 (닉네임 정보 확인)
            println("작성자 닉네임: ${author.nickname}")

            // 작성자를 팔로우하는 사용자들 가져오기 (EntityGraph 적용)
            val followers = followRepository.findAllByFollowing(author)
            println("팔로워 수: ${followers.size}")

            // 각 팔로워에게 알림 전송
            followers.forEach { follow ->
                val follower = follow.follower!!

                // 팔로워 정보 확인
                println("팔로워 ID: ${follower.id}, 닉네임: ${follower.nickname}")

                val event = NotificationEvent(
                        receiver = follower,
                        sender = author,
                        type = NotificationType.NEW_POST,
                        content = "${author.nickname}님이 새 게시글을 작성했습니다: ${post.title}",
                        relatedId = post.id!! // 게시글 ID
                )
                // 비동기 처리를 위해 notificationService 사용
                notificationService.processNotificationAsync(event)
            }
        } catch (e: Exception) {
            // 알림 전송 실패 시 로그만 기록하고 계속 진행
            println("새 게시글 알림 전송 실패: ${e.message}")
            e.printStackTrace()
        }
    }
}
