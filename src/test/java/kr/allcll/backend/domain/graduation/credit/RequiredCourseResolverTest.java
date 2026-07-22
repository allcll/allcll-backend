package kr.allcll.backend.domain.graduation.credit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import kr.allcll.backend.domain.graduation.credit.dto.RequiredCourseResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RequiredCourseResolverTest {

    @Mock
    private RequiredCourseRepository requiredCourseRepository;

    @Test
    @DisplayName("학과별 필수 제외 설정은 전역 필수 설정보다 우선한다")
    void departmentExemptionOverridesGlobalRequiredCourse() {
        when(requiredCourseRepository.findByAdmissionYearAndDepts(2021, List.of("0", "3220")))
            .thenReturn(List.of(
                requiredCourse("0", true),
                requiredCourse("3220", false)
            ));

        List<RequiredCourseResponse> requiredCourses = new RequiredCourseResolver(requiredCourseRepository)
            .resolveRequiredCourses(2021, "3220")
            .getOrDefault(CategoryType.GENERAL_ELECTIVE, List.of());

        assertThat(requiredCourses).isEmpty();
    }

    private RequiredCourse requiredCourse(String deptCd, boolean required) {
        return new RequiredCourse(
            2021,
            21,
            deptCd,
            "테스트학과",
            CategoryType.GENERAL_ELECTIVE,
            "REQ001",
            "지정과목",
            null,
            "GROUP",
            required,
            ""
        );
    }
}
