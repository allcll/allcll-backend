package kr.allcll.backend.domain.graduation.check.excel;

import java.util.List;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;

public record CompletedCourseDto(
    String curiNo,             // 학수번호
    String curiNm,             // 교과목명
    CategoryType categoryType, // 이수구분 (변환됨)
    String selectedArea,       // 선택영역 (균형교양 영역)
    Double credits,            // 학점
    String grade,              // 등급 (성적)
    MajorScope majorScope      // 전공 구분
) {

    private static final List<String> NOT_EARNED_GRADES = List.of("F", "FA", "NP");

    public static CompletedCourseDto of(
        String curiNo,
        String curiNm,
        String categoryTypeRaw,
        String selectedArea,
        Double credits,
        String grade
    ) {
        return new CompletedCourseDto(
            curiNo,
            curiNm,
            convertCategoryType(categoryTypeRaw),
            selectedArea,
            credits,
            grade,
            determineMajorScope(categoryTypeRaw)
        );
    }

    public CompletedCourse toEntity(Long userId) {
        return new CompletedCourse(
            userId,
            curiNo,
            curiNm,
            categoryType,
            selectedArea,
            credits,
            grade,
            majorScope,
            isCreditEarned()
        );
    }

    // grade 기준 학점 인정 판별 메서드
    public boolean isCreditEarned() {
        if (grade == null || grade.isEmpty()) {
            return true;
        }
        return !NOT_EARNED_GRADES.contains(grade);
    }

    private static CategoryType convertCategoryType(String raw) {
        if (raw == null) {
            return null;
        }
        String stripped = raw.strip();
        return switch (stripped) {
            case "교필", "공필" -> CategoryType.COMMON_REQUIRED;
            case "균필" -> CategoryType.BALANCE_REQUIRED;
            case "기교", "기필" -> CategoryType.ACADEMIC_BASIC;
            case "교선", "교선1", "교선2" -> CategoryType.GENERAL_ELECTIVE;
            case "전필", "복필" -> CategoryType.MAJOR_REQUIRED;
            case "전선", "복선" -> CategoryType.MAJOR_ELECTIVE;
            case "전기" -> CategoryType.MAJOR_BASIC;
            default -> null;
        };
    }

    private static MajorScope determineMajorScope(String categoryTypeRaw) {
        if (categoryTypeRaw == null) {
            throw new AllcllException(AllcllErrorCode.EMPTY_REQUIRED_COLUMN);
        }
        String stripped = categoryTypeRaw.strip();
        if (stripped.contains("복선") || stripped.contains("복필")) {
            return MajorScope.SECONDARY;
        }
        return MajorScope.PRIMARY;
    }
}
