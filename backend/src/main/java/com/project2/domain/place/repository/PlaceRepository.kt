package com.project2.domain.place.repository

import com.project2.domain.place.entity.Place
import org.springframework.data.jpa.repository.JpaRepository

interface PlaceRepository : JpaRepository<Place, Long>
