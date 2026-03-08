package kr.allcll.backend.domain.graduation.credit;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.Getter;

@Getter
public enum CategoryType { // 이수구분
    COMMON_REQUIRED(List.of("교필", "공필")),                  // 공통교양필수
    BALANCE_REQUIRED(List.of("균필")),                    // 균형교양
    ACADEMIC_BASIC(List.of("기교", "기필")),                   // 학문기초교양
    GENERAL_ELECTIVE(List.of("교선", "교선1", "교선2")),        // 교양선택
    GENERAL(List.of("교양")),                             // 교양
    MAJOR_REQUIRED(List.of("전필", "복필")),                   // 전공필수
    MAJOR_ELECTIVE(List.of("전선", "복선")),                   // 전공선택
    MAJOR_BASIC(List.of("전기")),                         // 전공기초
    TOTAL_COMPLETION(List.of());                             // 전체 이수


    private final List<String> aliases;

    CategoryType(List<String> aliases) {
        this.aliases = aliases;
    }

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

        CategoryType categoryType = Arrays.stream(values())
            .filter(type -> type.aliases.contains(stripped))
            .findFirst()
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.CATEGORY_TYPE_NOT_FOUND));

        if (isNotMajorBasic(categoryType)) {
            return categoryType;
        }
        return normalizeMajorBasic(admissionYear);
    }

    private static boolean isNotMajorBasic(CategoryType categoryType) {
        return !isMajorBasic(categoryType);
    }

    private static boolean isMajorBasic(CategoryType categoryType) {
        return MAJOR_BASIC.equals(categoryType);
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
