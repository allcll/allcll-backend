package kr.allcll.backend.domain.graduation.check.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import kr.allcll.backend.domain.graduation.certification.CodingTargetType;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCoursePersistenceService;
import kr.allcll.backend.domain.graduation.check.result.dto.CompletedCoursesResponse;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import kr.allcll.backend.fixture.GraduationDepartmentInfoFixture;
import kr.allcll.backend.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class GraduationCheckServiceTest {

    @Autowired
    private CompletedCoursePersistenceService completedCoursePersistenceService;

    @Autowired
    private GraduationCheckService graduationCheckService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("빈환값의 개수와 내용을 확인한다.")
    void checkAllEarnedCoursesContents() {
        // given
        GraduationDepartmentInfo graduationDepartmentInfo = GraduationDepartmentInfoFixture
            .createDepartmentInfo(2021, CodingTargetType.CODING_MAJOR);
        User user = UserFixture.singleMajorUser(2021, graduationDepartmentInfo);
        userRepository.save(user);
        CompletedCourseDto completedCourseDtoA = CompletedCourseDto.of("123456",
            "과목명A",
            "기교",
            "selectedArea",
            3.0,
            "A+");

        CompletedCourseDto completedCourseDtoB = CompletedCourseDto.of("654321",
            "과목명B",
            "전필",
            "selectedArea",
            3.0,
            "FA");
        completedCoursePersistenceService.saveAllCompletedCourse(
            user.getId(),
            List.of(completedCourseDtoA, completedCourseDtoB)
        );

        // when
        CompletedCoursesResponse allEarnedCourses = graduationCheckService.getAllEarnedCourses(user.getId());

        // then
        assertThat(allEarnedCourses.value()).hasSize(2)
            .extracting(
                "curiNo", "curiNm", "isEarned"
            )
            .containsExactlyInAnyOrder(
                tuple("123456", "과목명A", true),
                tuple("654321", "과목명B", false)
            );
    }
}
