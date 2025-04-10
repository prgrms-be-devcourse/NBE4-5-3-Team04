package com.project2.domain.member.integration.service

import com.project2.domain.member.enums.Provider
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.member.service.MemberService
import com.project2.global.util.ImageService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@Import(MemberServiceIntegrationTest.MockImageServiceConfig::class)
class MemberServiceIntegrationTest {

    @TestConfiguration
    class MockImageServiceConfig {
        @Bean
        @Primary
        fun mockImageService(): ImageService = Mockito.mock(ImageService::class.java)
    }

    // 실제 Bean 주입
    @Autowired
    lateinit var memberService: MemberService

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var imageService: ImageService

    @Test
    fun `회원가입 시 DB에 저장되고 profileImageUrl이 설정된다`() {
        // given
        val email = "test@example.com"
        val nickname = "테스트유저"
        val imageUrl = "https://cdn.example.com/image.jpg"
        val provider = Provider.GOOGLE

        whenever(imageService.downloadProfileImage(eq(imageUrl), any()))
                .thenReturn("/images/test-profile.png")

        // when
        val saved = memberService.signUp(email, nickname, imageUrl, provider)

        // then
        val found = memberRepository.findById(saved.id!!)
        assertThat(found).isPresent
        assertThat(found.get().profileImageUrl).isEqualTo("/images/test-profile.png")
    }
}
