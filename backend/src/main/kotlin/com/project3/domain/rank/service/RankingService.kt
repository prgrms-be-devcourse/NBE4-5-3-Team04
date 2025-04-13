package com.project3.domain.rank.service

import com.project3.domain.place.enums.Region
import com.project3.domain.post.dto.PostResponseDTO
import com.project3.domain.rank.dto.PopularPlaceResponseDTO
import com.project3.domain.rank.dto.RegionRankingResponseDTO
import com.project3.domain.rank.enums.RankingPeriod
import com.project3.domain.rank.repository.RankingRepository
import com.project3.global.security.SecurityUser
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@RequiredArgsConstructor
class RankingService(
        private val rankingRepository: RankingRepository
) {

    // 전국 인기 장소 조회
    @Transactional
    fun getPopularPlaces(
            period: RankingPeriod,
            placeName: String?,
            pageable: Pageable
    ): Page<PopularPlaceResponseDTO> {
        val startDate = period.getStartDate()
        return rankingRepository.findPopularPlaces(startDate, placeName, pageable)
                .map { it.toResponse() }
    }

    // 인기 지역 랭킹 조회
    @Transactional
    fun getRegionRankings(
            period: RankingPeriod,
            pageable: Pageable
    ): Page<RegionRankingResponseDTO> {
        val startDate = period.getStartDate()
        return rankingRepository.findRegionRankings(startDate, pageable)
                .map { it.toResponse() }
    }

    // 특정 지역의 게시글 목록 조회
    @Transactional
    fun getPostsByRegion(
            region: Region,
            period: RankingPeriod,
            pageable: Pageable,
            actor: SecurityUser
    ): Page<PostResponseDTO> {
        val startDate = period.getStartDate()
        return rankingRepository.findPostsByRegion(region, startDate, pageable)
                .map { PostResponseDTO(it, actor) }
    }

    // 특정 장소의 게시글 목록 조회
    @Transactional
    fun getPostsByPlace(
            placeId: Long,
            period: RankingPeriod,
            pageable: Pageable,
            actor: SecurityUser
    ): Page<PostResponseDTO> {
        val startDate = period.getStartDate()
        return rankingRepository.findPostsByPlace(placeId, startDate, pageable)
                .map { PostResponseDTO(it, actor) }
    }
}