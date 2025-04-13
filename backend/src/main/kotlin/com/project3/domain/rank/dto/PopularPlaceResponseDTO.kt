package com.project3.domain.rank.dto

data class PopularPlaceResponseDTO(
        val placeId: Long,
        val placeName: String,
        val region: String,
        val likeCount: Long,
        val scrapCount: Long,
        val postCount: Long
)