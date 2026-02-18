package kr.allcll.backend.domain.graduation.credit;

import static org.assertj.core.api.Assertions.assertThat;
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
    private RequiredCourseResolver requiredCourseResolver;

    @MockitoBean
    private CourseReplacementRepository courseReplacementRepository;

    @Test
    @DisplayName("ACADEMIC_BASIC이 아닌 과목은 검사 없이 통과한다")
    void returnTrueWhenNotAcademicBasic() {
        // given
        String departmentName = "컴퓨터공학과";
        Integer admissionYear = 2021;
        CompletedCourseDto course = new CompletedCourseDto(
            "123456",
            "알고리즘및실습",
            CategoryType.MAJOR_BASIC,
            "",
            3.0,
            "A+",
            MajorScope.PRIMARY
        );
        CreditCriterion criterion = createAcademicBasicCreditCriterion(departmentName, admissionYear);

        // when
        boolean result = academicBasicPolicy.isRecentMajorAcademicBasic(course, criterion);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("필수 과목 리스트에 존재하는 과목이면 true를 반환한다")
    void returnTrueWhenDirectMatch() {
        // given
        String courseName = "공학설계기초";
        String departmentName = "컴퓨터공학과";
        Integer admissionYear = 2021;
        CompletedCourseDto course = new CompletedCourseDto(
            "123456",
            courseName,
            CategoryType.ACADEMIC_BASIC,
            "",
            3.0,
            "A+",
            MajorScope.PRIMARY
        );
        CreditCriterion criterion = createAcademicBasicCreditCriterion(departmentName, admissionYear);

        given(
            requiredCourseResolver.findRequiredCourseNames(
                departmentName,
                admissionYear,
                CategoryType.ACADEMIC_BASIC)
        ).willReturn(List.of("공학설계기초", "기초미적분학"));

        // when
        boolean result = academicBasicPolicy.isRecentMajorAcademicBasic(course, criterion);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("필수 과목엔 없지만, 대체 과목의 레거시 이름이 필수 목록에 있으면 true를 반환한다")
    void returnTrueWhenReplacementMatch() {
        // given
        String attendCourseName = "새로운 공학설계기초";
        String oldCourseName = "공학설계기초";
        String departmentName = "컴퓨터공학과";
        Integer admissionYear = 2021;

        CompletedCourseDto course = new CompletedCourseDto(
            "123456",
            attendCourseName,
            CategoryType.ACADEMIC_BASIC,
            "",
            3.0,
            "A+",
            MajorScope.PRIMARY
        );
        CreditCriterion criterion = createAcademicBasicCreditCriterion(departmentName, admissionYear);

        given(
            requiredCourseResolver.findRequiredCourseNames(
                departmentName,
                admissionYear,
                CategoryType.ACADEMIC_BASIC)
        ).willReturn(List.of(oldCourseName));

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
    void returnFalseWhenNoMatchNoReplacement() {
        // given
        String departmentName = "컴퓨터공학과";
        Integer admissionYear = 2021;
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
        CreditCriterion criterion = createAcademicBasicCreditCriterion(departmentName, admissionYear);

        given(
            requiredCourseResolver.findRequiredCourseNames(
                departmentName,
                admissionYear,
                CategoryType.ACADEMIC_BASIC
            )
        ).willReturn(List.of("공학설계기초"));

        given(courseReplacementRepository.findRecentCourse(admissionYear, courseName))
            .willReturn(List.of());

        // when
        boolean result = academicBasicPolicy.isRecentMajorAcademicBasic(course, criterion);

        // then
        assertThat(result).isFalse();
    }

    private CreditCriterion createAcademicBasicCreditCriterion(String departmentName, Integer admissionYear) {
        return new CreditCriterion(
            admissionYear,
            21,
            MajorType.SINGLE,
            "3210",
            departmentName,
            MajorScope.PRIMARY,
            CategoryType.ACADEMIC_BASIC,
            9,
            true,
            ""
        );
    }
}
