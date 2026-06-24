package com.example.kwu_graduation.domain.simulation.model;

public enum CourseCategory {
    MAJOR,
    CULTURE,
    ETC,
    ANY;

    public static CourseCategory fromKlasCodeName(String codeName) {
        if (codeName == null || codeName.isBlank()) {
            return ETC;
        }
        if (codeName.startsWith("전")) {
            return MAJOR;
        }
        if (codeName.startsWith("교")) {
            return CULTURE;
        }
        return ETC;
    }

    public String recommendationName() {
        return switch (this) {
            case MAJOR -> "전공 선택 과목";
            case CULTURE -> "교양 선택 과목";
            case ETC, ANY -> "자유 선택 과목";
        };
    }
}
