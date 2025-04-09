package com.project2.domain.rank.controller

import com.project2.domain.place.enums.Region
import com.project2.domain.post.dto.PostResponseDTO
import com.project2.domain.rank.dto.PopularPlaceResponseDTO
import com.project2.domain.rank.dto.RegionRankingResponseDTO
import com.project2.domain.rank.enums.RankingPeriod
import com.project2.domain.rank.service.RankingService
import com.project2.global.dto.RsData
import com.project2.global.security.SecurityUser
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
class RankingController(
        private val rankingService: RankingService
) {

    // 전국 인기 장소 목록 조회
    @GetMapping("/places")
    fun getPopularPlaces(
            @RequestParam(defaultValue = "ONE_MONTH") period: RankingPeriod,
            @RequestParam(required = false) placeName: String?,
            @PageableDefault(size = 5) pageable: Pageable
    ): RsData<Page<PopularPlaceResponseDTO>> {
        return RsData("200", "인기 장소 조회 성공",
                rankingService.getPopularPlaces(period, placeName, pageable)
        )
    }

    // 장소의 게시글 목록 조회
    @GetMapping("/places/{placeId}/posts")
    fun getPostsByPlace(
            @PathVariable placeId: Long,
            @RequestParam(defaultValue = "ONE_MONTH") period: RankingPeriod,
            @PageableDefault(size = 5) pageable: Pageable,
            @AuthenticationPrincipal actor: SecurityUser
    ): RsData<Page<PostResponseDTO>> {
        return RsData("200", "장소의 게시글 조회 성공",
                rankingService.getPostsByPlace(placeId, period, pageable, actor)
        )
    }

    // 인기 지역 랭킹 조회
    @GetMapping("/regions")
    fun getRegionRankings(
            @RequestParam(defaultValue = "ONE_MONTH") period: RankingPeriod,
            @PageableDefault pageable: Pageable
    ): RsData<Page<RegionRankingResponseDTO>> {
        return RsData("200", "인기 지역 랭킹 조회 성공",
                rankingService.getRegionRankings(period, pageable)
        )
    }

    // 지역의 게시글 목록 조회
    @GetMapping("/regions/{region}/posts")
    fun getPostsByRegion(
            @PathVariable region: Region,
            @RequestParam(defaultValue = "ONE_MONTH") period: RankingPeriod,
            @PageableDefault pageable: Pageable,
            @AuthenticationPrincipal actor: SecurityUser
    ): RsData<Page<PostResponseDTO>> {
        return RsData("200", "${region.krRegion}의 게시글 조회 성공",
                rankingService.getPostsByRegion(region, period, pageable, actor)
        )
    }
}