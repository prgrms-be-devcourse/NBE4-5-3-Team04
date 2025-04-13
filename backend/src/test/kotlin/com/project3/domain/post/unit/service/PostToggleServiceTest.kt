package com.project3.domain.post.unit.service

import com.project3.domain.member.entity.Member
import com.project3.domain.post.dto.toggle.LikeResponseDTO
import com.project3.domain.post.dto.toggle.ScrapResponseDTO
import com.project3.domain.post.entity.Likes
import com.project3.domain.post.entity.Post
import com.project3.domain.post.entity.Scrap
import com.project3.domain.post.mapper.ToggleMapper
import com.project3.domain.post.repository.LikesRepository
import com.project3.domain.post.repository.PostRepository
import com.project3.domain.post.repository.ScrapRepository
import com.project3.domain.member.repository.FollowRepository
import com.project3.domain.member.repository.MemberRepository
import com.project3.domain.post.service.PostToggleService
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.transaction.annotation.Transactional
import java.util.*

@ExtendWith(MockitoExtension::class)
@Transactional
internal class PostToggleServiceTest {
    @InjectMocks
    private val postToggleService: PostToggleService? = null

    @Mock
    private val likesRepository: LikesRepository? = null

    @Mock
    private val scrapRepository: ScrapRepository? = null

    @Mock
    private val postRepository: PostRepository? = null

    @Mock
    private val toggleMapper: ToggleMapper? = null

    @Mock
    private val followRepository: FollowRepository? = null

    @Mock
    private val memberRepository: MemberRepository? = null

    private val userId = 1L
    private val postId = 10L

    @Test
    @DisplayName("좋아요가 이미 있는 경우 삭제하고 상태 반환")
    fun toggleLikes_removeLike() {
        // Given
        val post = Post().apply { id = postId }
        Mockito.`when`(postRepository!!.findById(postId)).thenReturn(Optional.of(post))
        Mockito.`when`(likesRepository!!.existsByPostIdAndMemberId(postId, userId)).thenReturn(true)
        Mockito.`when`(likesRepository.countByPostId(postId)).thenReturn(0)

        // When
        val result = postToggleService!!.toggleLikes(userId, postId)

        // Then
        Assertions.assertEquals("200", result.code)
        Assertions.assertEquals("좋아요 상태 변경 완료", result.msg)
        Assertions.assertFalse(result.data.liked)
        Assertions.assertEquals(0, result.data.likeCount)
        Mockito.verify(likesRepository, Mockito.never()).save(ArgumentMatchers.any(Likes::class.java))
    }

    @Test
    @DisplayName("스크랩이 없는 경우 추가하고 상태 반환")
    fun toggleScrap_addScrap() {
        // Given
        val mockPost = Post().apply { id = postId }
        val mockMember = Member().apply { id = userId }
        Mockito.`when`(scrapRepository!!.existsByPostIdAndMemberId(postId, userId)).thenReturn(false)
        Mockito.`when`(postRepository!!.getReferenceById(postId)).thenReturn(mockPost)
        Mockito.`when`(toggleMapper!!.toScrap(userId, mockPost))
            .thenReturn(Scrap(post = mockPost, member = mockMember))
        Mockito.`when`(scrapRepository.countByPostId(postId)).thenReturn(1)

        // When
        val result = postToggleService!!.toggleScrap(userId, postId)

        // Then
        Assertions.assertEquals("200", result.code)
        Assertions.assertEquals("스크랩 상태 변경 완료", result.msg)
        Assertions.assertTrue(result.data.scrapped)
        Assertions.assertEquals(1, result.data.scrapCount)
        Mockito.verify(scrapRepository).save(ArgumentMatchers.any(Scrap::class.java))
    }

    @Test
    @DisplayName("스크랩이 이미 있는 경우 삭제하고 상태 반환")
    fun toggleScrap_removeScrap() {
        // Given
        Mockito.`when`(scrapRepository!!.existsByPostIdAndMemberId(postId, userId)).thenReturn(true)
        Mockito.`when`(scrapRepository.countByPostId(postId)).thenReturn(0)

        // When
        val result = postToggleService!!.toggleScrap(userId, postId)

        // Then
        Assertions.assertEquals("200", result.code)
        Assertions.assertEquals("스크랩 상태 변경 완료", result.msg)
        Assertions.assertFalse(result.data.scrapped)
        Assertions.assertEquals(0, result.data.scrapCount)
        Mockito.verify(scrapRepository, Mockito.never()).save(ArgumentMatchers.any(Scrap::class.java))
    }

    @Test
    @DisplayName("스크랩 추가 시 게시물이 존재하지 않으면 예외 발생")
    fun toggleScrap_postNotFound() {
        // Given
        Mockito.`when`(scrapRepository!!.existsByPostIdAndMemberId(postId, userId)).thenReturn(false)
        Mockito.`when`(postRepository!!.getReferenceById(postId)).thenThrow(EntityNotFoundException("게시물을 찾을 수 없습니다."))

        // When & Then
        val exception = Assertions.assertThrows(
            EntityNotFoundException::class.java
        ) { postToggleService!!.toggleScrap(userId, postId) }

        Assertions.assertEquals("게시물을 찾을 수 없습니다.", exception.message)
    }

    @Test
    @DisplayName("좋아요 상태 조회 - 존재하는 경우")
    fun likeStatus_exists() {
        // Given
        Mockito.`when`(likesRepository!!.getLikeStatus(postId, userId)).thenReturn(LikeResponseDTO(true, 5))

        // When
        val result = likesRepository.getLikeStatus(postId, userId)

        // Then
        Assertions.assertTrue(result!!.liked)
        Assertions.assertEquals(5, result.likeCount)
    }

    @Test
    @DisplayName("좋아요 상태 조회 - 존재하지 않는 경우")
    fun likeStatus_notExists() {
        // Given
        Mockito.`when`(likesRepository!!.getLikeStatus(postId, userId)).thenReturn(LikeResponseDTO(false, 0))

        // When
        val result = likesRepository.getLikeStatus(postId, userId)

        // Then
        Assertions.assertFalse(result!!.liked)
        Assertions.assertEquals(0, result.likeCount)
    }

    @Test
    @DisplayName("스크랩 상태 조회 - 존재하는 경우")
    fun scrapStatus_exists() {
        // Given
        Mockito.`when`(scrapRepository!!.getScrapStatus(postId, userId)).thenReturn(ScrapResponseDTO(true, 3))

        // When
        val result = scrapRepository.getScrapStatus(postId, userId)

        // Then
        Assertions.assertTrue(result!!.scrapped)
        Assertions.assertEquals(3, result.scrapCount)
    }

    @Test
    @DisplayName("스크랩 상태 조회 - 존재하지 않는 경우")
    fun scrapStatus_notExists() {
        // Given
        Mockito.`when`(scrapRepository!!.getScrapStatus(postId, userId)).thenReturn(ScrapResponseDTO(false, 0))

        // When
        val result = scrapRepository.getScrapStatus(postId, userId)

        // Then
        Assertions.assertFalse(result!!.scrapped)
        Assertions.assertEquals(0, result.scrapCount)
    }
}