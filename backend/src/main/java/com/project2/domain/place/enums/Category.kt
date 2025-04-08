package com.project2.domain.place.enums;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public enum Category {
	MT1("대형마트"), CS2("편의점"), PS3("어린이집, 유치원"), SC4("학교"), AC5("학원"), PK6("주차장"), OL7("주유소, 충전소"), SW8("지하철역"), BK9(
		"은행"), CT1("문화시설"), AG2("중개업소"), PO3("공공기관"), AT4("관광명소"), AD5("숙박"), FD6("음식점"), CE7("카페"), HP8("병원"), PM9(
		"약국"), ETC("기타");

	public static final Map<String, Category> CATEGORY_MAP = new HashMap<>();

	Category(String krCategory) {
		this.krCategory = krCategory;
	}
	// 카테고리 한글명을 받아 코드로 변환 해주주는 부분.
	public final String krCategory;

	static {
		for (Category category : Category.values()) {
			CATEGORY_MAP.put(category.krCategory, category);
		}
	}

	public static Category fromKrCategory(String krCategory) {
		return CATEGORY_MAP.getOrDefault(krCategory, ETC);
	}

}