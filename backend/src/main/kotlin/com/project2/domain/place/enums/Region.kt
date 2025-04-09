package com.project2.domain.place.enums

enum class Region(val krRegion: String) {
    SEOUL("서울"), BUSAN("부산"), DAEGU("대구"), INCHEON("인천"), GWANGJU("광주"), DAEJEON("대전"), ULSAN("울산"), SEJONG("세종특별자치시"), GYEONGGI(
        "경기"
    ),
    GANGWON("강원특별자치도"), CHUNGBUK("충북"), CHUNGNAM("충남"), JEONBUK("전북특별자치도"), JEONNAM("전남"), GYEONGBUK("경북"), GYEONGNAM("경남"), JEJU(
        "제주특별자치도"
    ),
    ETC("기타");

    companion object {
        private val REGION_MAP: Map<String, Region> = entries.associateBy { it.krRegion }

        @JvmStatic
        fun fromKrRegion(krRegion: String): Region {
            return REGION_MAP[krRegion] ?: ETC
        }
    }
}