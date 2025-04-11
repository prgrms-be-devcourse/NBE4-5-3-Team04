package com.project2.domain.place.service

import com.project2.domain.place.entity.Place
import com.project2.domain.place.enums.Category.Companion.fromKrCategory
import com.project2.domain.place.enums.Region.Companion.fromKrRegion
import com.project2.domain.place.repository.PlaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlaceService(
    private val placeRepository: PlaceRepository
) {
    @Transactional
    fun savePlace(
        placeId: Long, name: String, latitude: Double, longitude: Double, region: String, category: String
    ): Place {
        val regionEnum = fromKrRegion(region)
        val categoryEnum = fromKrCategory(category)

        return placeRepository.findById(placeId).orElseGet {
            val newPlace = Place(
                id = placeId,
                name = name,
                latitude = latitude,
                longitude = longitude,
                region = regionEnum,
                category = categoryEnum
            )
            placeRepository.save(newPlace)
        }
    }
}
