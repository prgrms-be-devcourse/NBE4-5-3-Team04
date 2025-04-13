package com.project3.domain.place.integration

import com.project3.domain.place.entity.Place
import com.project3.domain.place.enums.Category
import com.project3.domain.place.enums.Region
import com.project3.domain.place.repository.PlaceRepository
import com.project3.domain.place.service.PlaceService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class PlaceIntegrationTest @Autowired constructor(
    private val placeService: PlaceService,
    private val placeRepository: PlaceRepository
) {

    private val testPlaceId = 1L
    private val name = "Sample Place"
    private val latitude = 37.5665
    private val longitude = 126.9780
    private val region = "서울"
    private val category = "맛집"

    @BeforeEach
    fun setUp() {
        placeRepository.deleteAll()
    }

    @Test
    @DisplayName("새로운 장소 저장 - 기존에 존재하지 않을 때")
    fun saveNewPlace_whenNotExist() {
        val savedPlace = placeService.savePlace(testPlaceId, name, latitude, longitude, region, category)

        assertNotNull(savedPlace)
        assertEquals(testPlaceId, savedPlace.id)
        assertEquals(name, savedPlace.name)
        assertEquals(latitude, savedPlace.latitude)
        assertEquals(longitude, savedPlace.longitude)
        assertEquals(Region.fromKrRegion(region), savedPlace.region)
        assertEquals(Category.fromKrCategory(category), savedPlace.category)
    }

    @Test
    @DisplayName("기존 장소 반환 - 이미 존재할 때")
    fun returnExistingPlace_whenAlreadyExists() {
        val preSaved = Place(
            id = testPlaceId,
            name = name,
            latitude = latitude,
            longitude = longitude,
            region = Region.fromKrRegion(region),
            category = Category.fromKrCategory(category)
        )
        placeRepository.save(preSaved)

        val result = placeService.savePlace(testPlaceId, "다른 이름", 0.0, 0.0, region, category)

        assertEquals(preSaved.id, result.id)
        assertEquals(name, result.name)
        assertEquals(latitude, result.latitude)
        assertEquals(longitude, result.longitude)
    }
}