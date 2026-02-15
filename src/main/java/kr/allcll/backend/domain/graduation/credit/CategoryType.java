package kr.allcll.backend.domain.graduation.credit;

import java.util.EnumSet;
import java.util.Set;

public enum CategoryType { // 이수구분
    COMMON_REQUIRED,       // 공통교양필수
    BALANCE_REQUIRED,      // 균형교양
    ACADEMIC_BASIC,        // 학문기초교양
    GENERAL_ELECTIVE,      // 교양선택
    MAJOR_REQUIRED,        // 전공필수
    MAJOR_ELECTIVE,        // 전공선택
    MAJOR_BASIC,           // 전공기초
    TOTAL_COMPLETION,      // 전체 이수
    ;

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
}
