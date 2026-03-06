package kr.allcll.backend.domain.graduation.credit;

import java.util.EnumSet;
import java.util.Set;

public enum CategoryType { // 이수구분
    COMMON_REQUIRED,       // 공통교양필수
    BALANCE_REQUIRED,      // 균형교양
    ACADEMIC_BASIC,        // 학문기초교양
    GENERAL_ELECTIVE,      // 교양선택
    GENERAL,               // 교양
    MAJOR_REQUIRED,        // 전공필수
    MAJOR_ELECTIVE,        // 전공선택
    MAJOR_BASIC,           // 전공기초
    TOTAL_COMPLETION,      // 전체 이수
    ;

    private static final int MAJOR_BASIC_INTRODUCED_ADMISSION_YEAR = 2024;

    private static final Set<CategoryType> MAJOR_CATEGORIES = EnumSet.of(
        MAJOR_REQUIRED,
        MAJOR_ELECTIVE
    );

    public boolean isMajorCategory() {
        return MAJOR_CATEGORIES.contains(this);
    }

    public boolean isNonMajorCategory() {
        return !isMajorCategory();
    }

    public static CategoryType fromRaw(String categoryTypeRaw, int admissionYear) {
        String stripped = categoryTypeRaw.strip();
        return switch (stripped) {
            case "교필", "공필" -> COMMON_REQUIRED;
            case "균필" -> BALANCE_REQUIRED;
            case "기교", "기필" -> ACADEMIC_BASIC;
            case "교선", "교선1", "교선2" -> GENERAL_ELECTIVE;
            case "교양" -> GENERAL;
            case "전필", "복필" -> MAJOR_REQUIRED;
            case "전선", "복선" -> MAJOR_ELECTIVE;
            case "전기" -> normalizeMajorBasic(admissionYear);
            default -> null;
        };
    }

    private static CategoryType normalizeMajorBasic(int admissionYear) {
        if (shouldConvertMajorBasicAsAcademicBasic(admissionYear)){
            return ACADEMIC_BASIC;
        }
        return MAJOR_BASIC;
    }

    private static boolean shouldConvertMajorBasicAsAcademicBasic(int admissionYear) {
        return admissionYear < MAJOR_BASIC_INTRODUCED_ADMISSION_YEAR;
    }
}
