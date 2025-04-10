package com.project2.domain.member.unit.service

import com.project2.domain.member.entity.Member
import com.project2.domain.member.enums.Provider
import com.project2.domain.member.repository.MemberRepository
import com.project2.domain.member.service.MemberService
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

    private val testEmail = "test@example.com"
    private val testNickname = "TestUser"
    private val testProvider = Provider.GOOGLE
    private val testProfileImageUrl = "/profile.jpg"

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
        mockMember = Member().apply {
            id = 1L
            email = testEmail
            nickname = testNickname
            provider = testProvider
            profileImageUrl = testProfileImageUrl
        }

        actor = SecurityUser(mockMember)
    }

    @Test
    @DisplayName("로그인을 수행된다.")
    fun `member exists - returns existing member`() {
        // Given
        `when`(memberRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockMember))

        // When
        val result = memberService.findByEmail(testEmail)

        // Then
        assertThat(result).isPresent
        assertThat(result.get().email).isEqualTo(testEmail)
        assertThat(result.get().nickname).isEqualTo(testNickname)

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
        val result = memberService.signUp(testEmail, testNickname, testProfileImageUrl, testProvider)

        // Then
        assertThat(result).isNotNull
        assertThat(result.email).isEqualTo(testEmail)
        assertThat(result.provider).isEqualTo(testProvider)
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
        memberService.updateNickname(memberId, newNickname)

        // Then
        assertThat(mockMember.nickname).isEqualTo(newNickname)
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
        memberService.updateProfileImageUrl(memberId, mockFile)

        // Then
        assertThat(mockMember.profileImageUrl).isEqualTo(mockFilePath)
        verify(memberRepository).findById(memberId)
        verify(imageService).storeProfileImage(memberId, mockFile)
    }
}