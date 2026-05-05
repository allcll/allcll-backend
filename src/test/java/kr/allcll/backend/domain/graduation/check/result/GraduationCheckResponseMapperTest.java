package kr.allcll.backend.domain.graduation.check.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCategory;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class GraduationCheckResponseMapperTest {

    private final GraduationCheckResponseMapper graduationCheckResponseMapper =
        new GraduationCheckResponseMapper(null,
            null,
            null,
            null,
            null,
            null
        );

    @Test
    @DisplayName("전필 초과 학점이 있으면 전선 카테고리를 생성해서 넘긴다")
    void createMajorElectiveWhenMajorRequiredOverflows() {
        List<GraduationCategory> result = invokeReallocateCredits(List.of(
            category(MajorScope.PRIMARY, CategoryType.MAJOR_REQUIRED, 36.0, 33),
            category(MajorScope.PRIMARY, CategoryType.GENERAL_ELECTIVE, 21.0, 21)
        ));

        assertAll(
            () -> assertThat(findCategory(result, CategoryType.MAJOR_REQUIRED).earnedCredits()).isEqualTo(33.0),
            () -> assertThat(findCategory(result, CategoryType.MAJOR_ELECTIVE).earnedCredits()).isEqualTo(3.0),
            () -> assertThat(findCategory(result, CategoryType.GENERAL_ELECTIVE).earnedCredits()).isEqualTo(21.0)
        );
    }

    @Test
    @DisplayName("전선 초과 학점이 있으면 교양 카테고리를 생성해서 넘긴다")
    void createGeneralWhenMajorElectiveOverflows() {
        List<GraduationCategory> result = invokeReallocateCredits(List.of(
            category(MajorScope.PRIMARY, CategoryType.MAJOR_REQUIRED, 36.0, 33),
            category(MajorScope.PRIMARY, CategoryType.MAJOR_ELECTIVE, 41.0, 39)
        ));

        assertAll(
            () -> assertThat(findCategory(result, CategoryType.MAJOR_REQUIRED).earnedCredits()).isEqualTo(33.0),
            () -> assertThat(findCategory(result, CategoryType.MAJOR_ELECTIVE).earnedCredits()).isEqualTo(39.0),
            () -> assertThat(findCategory(result, CategoryType.GENERAL).earnedCredits()).isEqualTo(5.0)
        );
    }

    @Test
    @DisplayName("초과 학점이 없으면 빈 전선이나 교양 카테고리를 만들지 않는다")
    void doNotCreateEmptyCategoriesWhenNoOverflow() {
        List<GraduationCategory> result = invokeReallocateCredits(List.of(
            category(MajorScope.PRIMARY, CategoryType.MAJOR_REQUIRED, 33.0, 33),
            category(MajorScope.PRIMARY, CategoryType.GENERAL_ELECTIVE, 21.0, 21)
        ));

        assertAll(
            () -> assertThat(findNullableCategory(result, CategoryType.MAJOR_ELECTIVE)).isNull(),
            () -> assertThat(findNullableCategory(result, CategoryType.GENERAL)).isNull(),
            () -> assertThat(findCategory(result, CategoryType.MAJOR_REQUIRED).earnedCredits()).isEqualTo(33.0),
            () -> assertThat(findCategory(result, CategoryType.GENERAL_ELECTIVE).earnedCredits()).isEqualTo(21.0)
        );
    }

    @Test
    @DisplayName("복수전공의 전선 초과 학점은 마지막에 PRIMARY GENERAL로 합산한다")
    void mergeGeneralOverflowIntoPrimaryAtTheEnd() {
        List<GraduationCategory> result = invokeReallocateCredits(List.of(
            category(MajorScope.PRIMARY, CategoryType.MAJOR_REQUIRED, 36.0, 33),
            category(MajorScope.PRIMARY, CategoryType.MAJOR_ELECTIVE, 42.0, 39),
            category(MajorScope.SECONDARY, CategoryType.MAJOR_REQUIRED, 18.0, 15),
            category(MajorScope.SECONDARY, CategoryType.MAJOR_ELECTIVE, 24.0, 21),
            category(MajorScope.PRIMARY, CategoryType.GENERAL, 10.0, 12)
        ));

        assertAll(
            () -> assertThat(findCategory(result, MajorScope.PRIMARY, CategoryType.MAJOR_REQUIRED).earnedCredits()).isEqualTo(33.0),
            () -> assertThat(findCategory(result, MajorScope.PRIMARY, CategoryType.MAJOR_ELECTIVE).earnedCredits()).isEqualTo(39.0),
            () -> assertThat(findCategory(result, MajorScope.SECONDARY, CategoryType.MAJOR_REQUIRED).earnedCredits()).isEqualTo(15.0),
            () -> assertThat(findCategory(result, MajorScope.SECONDARY, CategoryType.MAJOR_ELECTIVE).earnedCredits()).isEqualTo(21.0),
            () -> assertThat(findCategory(result, MajorScope.PRIMARY, CategoryType.GENERAL).earnedCredits()).isEqualTo(22.0),
            () -> assertThat(findNullableCategory(result, MajorScope.SECONDARY, CategoryType.GENERAL)).isNull()
        );
    }

    @SuppressWarnings("unchecked")
    private List<GraduationCategory> invokeReallocateCredits(List<GraduationCategory> categories) {
        return (List<GraduationCategory>) ReflectionTestUtils.invokeMethod(
            graduationCheckResponseMapper,
            "reallocateCredits",
            categories
        );
    }

    private GraduationCategory category(
        MajorScope majorScope,
        CategoryType categoryType,
        double earnedCredits,
        int requiredCredits
    ) {
        double remainingCredits = Math.max(requiredCredits - earnedCredits, 0);
        return new GraduationCategory(
            majorScope,
            categoryType,
            earnedCredits,
            requiredCredits,
            remainingCredits,
            null,
            null,
            null,
            remainingCredits <= 0
        );
    }

    private GraduationCategory findCategory(List<GraduationCategory> categories, CategoryType categoryType) {
        return findCategory(categories, MajorScope.PRIMARY, categoryType);
    }

    private GraduationCategory findCategory(
        List<GraduationCategory> categories,
        MajorScope majorScope,
        CategoryType categoryType
    ) {
        return categories.stream()
            .filter(category -> category.majorScope() == majorScope)
            .filter(category -> category.categoryType() == categoryType)
            .findFirst()
            .orElseThrow();
    }

    private GraduationCategory findNullableCategory(List<GraduationCategory> categories, CategoryType categoryType) {
        return findNullableCategory(categories, MajorScope.PRIMARY, categoryType);
    }

    private GraduationCategory findNullableCategory(
        List<GraduationCategory> categories,
        MajorScope majorScope,
        CategoryType categoryType
    ) {
        return categories.stream()
            .filter(category -> category.majorScope() == majorScope)
            .filter(category -> category.categoryType() == categoryType)
            .findFirst()
            .orElse(null);
    }
}
