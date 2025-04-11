package com.project2.domain.post.unit.repository

import com.project2.domain.member.entity.Member
import com.project2.domain.post.entity.Post
import com.project2.domain.post.entity.Scrap
import com.project2.domain.post.repository.ScrapRepository
import com.project2.domain.post.repository.PostRepository
import com.project2.domain.member.repository.MemberRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
class PostToggleRepositoryTest @Autowired constructor(
    val scrapRepository: ScrapRepository,
    val postRepository: PostRepository,
    val memberRepository: MemberRepository
) {

    lateinit var member: Member
    lateinit var post: Post
    lateinit var scrap: Scrap

    @BeforeEach
    fun setUp() {
        member = memberRepository.save(Member(email = "test@example.com", nickname = "nickname"))

        val postTemp = Post().apply {
            title = "Sample"
            content = "Content"
        }

        postTemp.member = member;
        post = postRepository.save(postTemp);

        scrap = Scrap(post = post, member = member)
    }

    @Test
    @DisplayName("스크랩 존재 여부 확인")
    fun t1() {
        scrapRepository.save(scrap)
        val exists = scrapRepository.existsByPostIdAndMemberId(post.id, member.id)
        assertTrue(exists)
    }

    @Test
    @DisplayName("스크랩 삭제 쿼리 실행")
    fun t2 () {
        scrapRepository.save(scrap)
        val deleted = scrapRepository.toggleScrapIfExists(post.id, member.id)
        assertEquals(1, deleted)
    }

    @Test
    @DisplayName("스크랩 수 조회")
    fun t3 () {
        scrapRepository.save(scrap)
        val count = scrapRepository.countByPostId(post.id)
        assertEquals(1, count)
    }
}