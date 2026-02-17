package kr.allcll.backend.domain.graduation.credit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.List;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AcademicBasicPolicyTest {

    @Autowired
    private AcademicBasicPolicy academicBasicPolicy;

    @MockitoBean
    private RequiredCourseRepository requiredCourseRepository;

    @MockitoBean
    private CourseReplacementRepository courseReplacementRepository;

    @Test
    @DisplayName("학문기초교양이 아닌 과목은 검사 없이 통과한다")
    void returnTrue_When_NotAcademicBasic() {
        // given
        CompletedCourseDto course = new CompletedCourseDto(
            "123456",
            "알고리즘및실습",
            CategoryType.MAJOR_BASIC,
            "",
            3.0,
            "A+",
            MajorScope.PRIMARY
        );
        CreditCriterion criterion = createAcademicBasicCreditCriterion();

        // when
        boolean result = academicBasicPolicy.isRecentMajorAcademicBasic(course, criterion);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("필수 과목 리스트에 존재하는 과목이면 true를 반환한다")
    void returnTrue_When_DirectMatch() {
        // given
        String courseName = "공학설계기초";
        CompletedCourseDto course = new CompletedCourseDto(
            "123456",
            courseName,
            CategoryType.ACADEMIC_BASIC,
            "",
            3.0,
            "A+",
            MajorScope.PRIMARY
        );
        CreditCriterion criterion = createAcademicBasicCreditCriterion();

        given(requiredCourseRepository.findRequiredCourseNames("컴퓨터공학과", 2021, CategoryType.ACADEMIC_BASIC))
            .willReturn(List.of("공학설계기초", "기초미적분학"));

        // when
        boolean result = academicBasicPolicy.isRecentMajorAcademicBasic(course, criterion);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("필수 과목엔 없지만, 대체 과목의 레거시 이름이 필수 목록에 있으면 true를 반환한다")
    void returnTrue_When_ReplacementMatch() {
        // given
        String attendCourseName = "새로운 공학설계기초";
        String oldCourseName = "공학설계기초";

        CompletedCourseDto course = new CompletedCourseDto(
            "123456",
            attendCourseName,
            CategoryType.ACADEMIC_BASIC,
            "",
            3.0,
            "A+",
            MajorScope.PRIMARY
        );
        CreditCriterion criterion = createAcademicBasicCreditCriterion();

        given(requiredCourseRepository.findRequiredCourseNames("컴퓨터공학과", 2021, CategoryType.ACADEMIC_BASIC))
            .willReturn(List.of(oldCourseName));

        CourseReplacement replacement = new CourseReplacement(
            2021,
            21,
            oldCourseName,
            "currentCuriNo",
            attendCourseName,
            true,
            null
        );
        given(courseReplacementRepository.findRecentCourse(2021, attendCourseName))
            .willReturn(List.of(replacement));

        // when
        boolean result = academicBasicPolicy.isRecentMajorAcademicBasic(course, criterion);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("필수 과목에도 없고, 대체 과목 정보도 없으면 false를 반환한다")
    void returnFalse_When_NoMatch_NoReplacement() {
        // given
        String courseName = "건환공기초창의설계";
        CompletedCourseDto course = new CompletedCourseDto(
            "123456",
            courseName,
            CategoryType.ACADEMIC_BASIC,
            "",
            3.0,
            "A+",
            MajorScope.PRIMARY
        );
        CreditCriterion criterion = createAcademicBasicCreditCriterion();

        given(requiredCourseRepository.findRequiredCourseNames(any(), any(), any()))
            .willReturn(List.of("공학설계기초"));

        given(courseReplacementRepository.findRecentCourse(any(), eq(courseName)))
            .willReturn(List.of());

        // when
        boolean result = academicBasicPolicy.isRecentMajorAcademicBasic(course, criterion);

        // then
        assertThat(result).isFalse();
    }

    private CreditCriterion createAcademicBasicCreditCriterion() {
        return new CreditCriterion(
            2021,
            21,
            MajorType.SINGLE,
            "3210",
            "컴퓨터공학과",
            MajorScope.PRIMARY,
            CategoryType.ACADEMIC_BASIC,
            9,
            true,
            ""
        );
    }
}
