package com.project3.domain.rank.dto

import com.project3.domain.place.enums.Region

data class PopularPlaceDTO(
        val placeId: Long,
        val placeName: String,
        val region: Region,
        val likeCount: Long,
        val scrapCount: Long,
        val postCount: Long
) {
    fun toResponse(): PopularPlaceResponseDTO {
        return PopularPlaceResponseDTO(
                placeId = placeId,
                placeName = placeName,
                region = region.krRegion,
                likeCount = likeCount,
                scrapCount = scrapCount,
                postCount = postCount
        )
    }
}