package kr.allcll.backend.domain.graduation.check.excel;

import java.util.List;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;

public record CompletedCourseDto(
    String curiNo,             // 학수번호
    String curiNm,             // 교과목명
    String categoryTypeRaw, // 이수구분 (변환됨)
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
            categoryTypeRaw,
            selectedArea,
            credits,
            grade,
            determineMajorScope(categoryTypeRaw)
        );
    }

    public CompletedCourse toEntity(Long userId, int admissionYear) {
        return new CompletedCourse(
            userId,
            curiNo,
            curiNm,
            CategoryType.fromRaw(categoryTypeRaw, admissionYear),
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
