package com.project2.domain.place.dto;

import lombok.Getter;

@Getter
public class PlaceDTO {
	private final String placeName;
	private final String category;

	public PlaceDTO(String placeName, String category) {
		this.placeName = placeName;
		this.category = category;
	}
}
