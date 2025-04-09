package com.project2.domain.post.repository

import com.project2.domain.post.dto.toggle.LikeResponseDTO
import com.project2.domain.post.entity.Likes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface LikesRepository : JpaRepository<Likes?, Long?> {
    @Modifying
    @Query(
        """
        DELETE FROM Likes l
        WHERE l.post.id = :postId AND l.member.id = :memberId
        
        """
    )
    fun toggleLikeIfExists(@Param("postId") postId: Long?, @Param("memberId") memberId: Long?): Int

    @Query(
        """
        SELECT new com.project2.domain.post.dto.toggle.LikeResponseDTO(
            CASE WHEN COUNT(l) > 0 THEN true ELSE false END,
            CAST(COUNT(l2) AS integer)
        )
        FROM Likes l
        LEFT JOIN Likes l2 ON l2.post.id = l.post.id
        WHERE l.post.id = :postId AND l.member.id = :memberId
        
        """
    )
    fun getLikeStatus(@Param("postId") postId: Long?, @Param("memberId") memberId: Long?): LikeResponseDTO?

    @Query("SELECT COUNT(l) FROM Likes l WHERE l.post.id = :postId")
    fun countByPostId(@Param("postId") postId: Long?): Int

    @Query("SELECT COUNT(l) > 0 FROM Likes l WHERE l.post.id = :postId AND l.member.id = :memberId")
    fun existsByPostIdAndMemberId(@Param("postId") postId: Long?, @Param("memberId") memberId: Long?): Boolean
}