package kr.allcll.backend.domain.graduation.check.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRuleType;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResultRepository;
import kr.allcll.backend.domain.graduation.certification.CodingTargetType;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCoursePersistenceService;
import kr.allcll.backend.domain.graduation.check.result.dto.CompletedCoursesResponse;
import kr.allcll.backend.domain.graduation.check.result.dto.UpdateEnglishCertRequest;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import kr.allcll.backend.fixture.GraduationCheckCertResultFixture;
import kr.allcll.backend.fixture.GraduationDepartmentInfoFixture;
import kr.allcll.backend.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
class GraduationCheckServiceTest {

    @Autowired
    private CompletedCoursePersistenceService completedCoursePersistenceService;

    @Autowired
    private GraduationCheckService graduationCheckService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GraduationCheckCertResultRepository graduationCheckCertResultRepository;

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
        CompletedCoursesResponse allEarnedCourses = graduationCheckService.getAllCompletedCourses(user.getId());

        // then
        assertThat(allEarnedCourses.courses()).hasSize(2)
            .extracting(
                "curiNo", "curiNm", "isEarned"
            )
            .containsExactlyInAnyOrder(
                tuple("123456", "과목명A", true),
                tuple("654321", "과목명B", false)
            );
    }

    @Test
    @DisplayName("영어 인증을 true로 수정하면 passedCount와 만족 여부를 재계산한다.")
    void updateEnglishCertPass_true_recalculatesResult() {
        // given
        GraduationDepartmentInfo graduationDepartmentInfo = GraduationDepartmentInfoFixture
            .createDepartmentInfo(2021, CodingTargetType.CODING_MAJOR);
        User user = userRepository.save(UserFixture.singleMajorUser(2021, graduationDepartmentInfo));
        GraduationCheckCertResult certResult = GraduationCheckCertResultFixture.createCertResult(
            user,
            GraduationCertRuleType.BOTH_REQUIRED,
            false,
            false,
            true
        );
        graduationCheckCertResultRepository.save(certResult);

        // when
        graduationCheckService.updateEnglishCertPass(user.getId(), new UpdateEnglishCertRequest(true));
        GraduationCheckCertResult updated = graduationCheckCertResultRepository.findByUserId(user.getId()).orElseThrow();

        // then
        assertThat(updated.getIsEnglishCertPassed()).isTrue();
        assertThat(updated.getPassedCount()).isEqualTo(2);
        assertThat(updated.getRequiredPassCount()).isEqualTo(2);
        assertThat(updated.getIsSatisfied()).isTrue();
    }

    @Test
    @DisplayName("영어 인증을 false로 수정하면 passedCount와 만족 여부를 재계산한다.")
    void updateEnglishCertPass_false_recalculatesResult() {
        // given
        GraduationDepartmentInfo graduationDepartmentInfo = GraduationDepartmentInfoFixture
            .createDepartmentInfo(2021, CodingTargetType.CODING_MAJOR);
        User user = userRepository.save(UserFixture.singleMajorUser(2021, graduationDepartmentInfo));
        GraduationCheckCertResult certResult = GraduationCheckCertResultFixture.createCertResult(
            user,
            GraduationCertRuleType.BOTH_REQUIRED,
            true,
            false,
            true
        );
        graduationCheckCertResultRepository.save(certResult);

        // when
        graduationCheckService.updateEnglishCertPass(user.getId(), new UpdateEnglishCertRequest(false));
        GraduationCheckCertResult updated = graduationCheckCertResultRepository.findByUserId(user.getId()).orElseThrow();

        // then
        assertThat(updated.getIsEnglishCertPassed()).isFalse();
        assertThat(updated.getPassedCount()).isEqualTo(1);
        assertThat(updated.getRequiredPassCount()).isEqualTo(2);
        assertThat(updated.getIsSatisfied()).isFalse();
    }
}
