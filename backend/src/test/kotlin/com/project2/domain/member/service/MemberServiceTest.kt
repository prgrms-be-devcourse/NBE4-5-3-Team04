package com.project2.domain.member.service

import com.project2.domain.member.entity.Member
import com.project2.domain.member.enums.Provider
import com.project2.domain.member.repository.MemberRepository
import com.project2.global.security.SecurityUser
import com.project2.global.util.ImageService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.util.*

@ExtendWith(MockitoExtension::class)
class MemberServiceTest {

    private val email = "test@example.com"
    private val nickname = "TestUser"
    private val profileImageUrl = "/profile.jpg"
    private val provider = Provider.GOOGLE

    @Mock
    private lateinit var memberRepository: MemberRepository

    @Mock
    private lateinit var imageService: ImageService

    @InjectMocks
    private lateinit var memberService: MemberService

    private lateinit var mockMember: Member
    private lateinit var actor: SecurityUser

    @BeforeEach
    fun setUp() {
        mockMember = Member.builder()
                .id(1L)
                .email(email)
                .nickname(nickname)
                .provider(provider)
                .profileImageUrl(profileImageUrl)
                .build()

        actor = SecurityUser(mockMember)
    }

    @Test
    @DisplayName("로그인을 수행된다.")
    fun `member exists - returns existing member`() {
        // Given
        `when`(memberRepository.findByEmail(email)).thenReturn(Optional.of(mockMember))

        // When
        val result = memberService.findByEmail(email)

        // Then
        assertThat(result).isPresent
        assertThat(result.get().email).isEqualTo(email)
        assertThat(result.get().nickname).isEqualTo(nickname)

        verify(memberRepository, never()).save(any(Member::class.java))
    }

    @Test
    @DisplayName("회원 가입을 수행한다.")
    fun `member not exists - creates new member`() {
        // Given
        val mockedPath = "mocked/path/profile.png"
        `when`(imageService.downloadProfileImage(anyString(), anyLong())).thenReturn(mockedPath)
        `when`(memberRepository.save(any(Member::class.java))).thenReturn(mockMember)

        // When
        val result = memberService.signUp(email, nickname, profileImageUrl, provider)

        // Then
        assertThat(result).isNotNull
        assertThat(result.email).isEqualTo(email)
        assertThat(result.provider).isEqualTo(provider)
        assertThat(result.profileImageUrl).isEqualTo(mockedPath)

        verify(memberRepository).save(any(Member::class.java))
        verify(imageService).downloadProfileImage(anyString(), anyLong())
    }

    @Test
    @DisplayName("닉네임을 수정한다")
    fun `member exists - update nickname`() {
        // Given
        val memberId = 1L
        val newNickname = "newNickname"

        `when`(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember))

        // When
        val updatedMember = memberService.updateNickname(memberId, newNickname)

        // Then
        assertThat(updatedMember.nickname).isEqualTo(newNickname)
        verify(memberRepository).findById(memberId)
    }

    @Test
    @DisplayName("프로필 이미지를 수정한다")
    fun `member exists - update profile image url`() {
        // Given
        val memberId = 1L
        val mockFile: MultipartFile = MockMultipartFile(
                "profileImage",
                "profile.png",
                "image/png",
                "dummy image content".toByteArray()
        )

        `when`(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember))
        val mockFilePath = "/uploads/profiles/1/1710208701234.png"
        `when`(imageService.storeProfileImage(memberId, mockFile)).thenReturn(mockFilePath)

        // When
        val updatedMember = memberService.updateProfileImageUrl(memberId, mockFile)

        // Then
        assertThat(updatedMember.profileImageUrl).isEqualTo(mockFilePath)
        verify(memberRepository).findById(memberId)
        verify(imageService).storeProfileImage(memberId, mockFile)
    }
}