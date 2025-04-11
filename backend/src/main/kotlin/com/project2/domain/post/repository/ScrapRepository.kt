package com.project2.domain.post.repository

import com.project2.domain.post.dto.toggle.ScrapResponseDTO
import com.project2.domain.post.entity.Scrap
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ScrapRepository : JpaRepository<Scrap?, Long?> {
    @Modifying
    @Query(
        """
			DELETE FROM Scrap s
			WHERE s.post.id = :postId AND s.member.id = :memberId
			
			"""
    )
    fun toggleScrapIfExists(@Param("postId") postId: Long?, @Param("memberId") memberId: Long?): Int

    @Query(
        """
			SELECT new com.project2.domain.post.dto.toggle.ScrapResponseDTO(
			    CASE WHEN COUNT(s) > 0 THEN true ELSE false END,
			    CAST(COUNT(s2) AS integer)
			)
			FROM Scrap s
			LEFT JOIN Scrap s2 ON s2.post.id = s.post.id
			WHERE s.post.id = :postId AND s.member.id = :memberId
			
			"""
    )
    fun getScrapStatus(@Param("postId") postId: Long?, @Param("memberId") memberId: Long?): ScrapResponseDTO?

    @Query("SELECT COUNT(s) FROM Scrap s WHERE s.post.id = :postId")
    fun countByPostId(@Param("postId") postId: Long?): Int

    @Query("SELECT COUNT(s) > 0 FROM Scrap s WHERE s.post.id = :postId AND s.member.id = :memberId")
    fun existsByPostIdAndMemberId(@Param("postId") postId: Long?, @Param("memberId") memberId: Long?): Boolean
}