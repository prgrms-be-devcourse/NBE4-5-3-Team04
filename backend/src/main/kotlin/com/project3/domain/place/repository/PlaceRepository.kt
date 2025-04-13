package com.project3.domain.place.repository

import com.project3.domain.place.entity.Place
import org.springframework.data.jpa.repository.JpaRepository

interface PlaceRepository : JpaRepository<Place, Long>
