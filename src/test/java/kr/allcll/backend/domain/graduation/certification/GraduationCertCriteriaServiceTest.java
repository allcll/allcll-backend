package kr.allcll.backend.domain.graduation.certification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.certification.dto.GraduationCertCriteriaResponse;
import kr.allcll.backend.domain.graduation.department.DeptGroup;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfoRepository;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import kr.allcll.backend.fixture.ClassicCertCriterionFixture;
import kr.allcll.backend.fixture.CodingCertCriterionFixture;
import kr.allcll.backend.fixture.EnglishCertCriterionFixture;
import kr.allcll.backend.fixture.UserFixture;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class GraduationCertCriteriaServiceTest {

    @Autowired
    private GraduationCertCriteriaService graduationCertCriteriaService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GraduationDepartmentInfoRepository departmentInfoRepository;

    @Autowired
    private GraduationCertRuleRepository certRuleRepository;

    @Autowired
    private EnglishCertCriterionRepository englishCertCriterionRepository;

    @Autowired
    private ClassicCertCriterionRepository classicCertCriterionRepository;

    @Autowired
    private CodingCertCriterionRepository codingCertCriterionRepository;

    @AfterEach
    void clean() {
        codingCertCriterionRepository.deleteAllInBatch();
        classicCertCriterionRepository.deleteAllInBatch();
        englishCertCriterionRepository.deleteAllInBatch();
        certRuleRepository.deleteAllInBatch();
        departmentInfoRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("18~22학번 단일 전공 영어 비전공자인 경우 졸업인증 기준 데이터 조회를 검증한다.")
    void getGraduationCertCriteria_bothRequired_nonMajorEnglish() {
        // given
        int admissionYear = 2022;
        String deptCd = "3220";

        certRuleRepository.saveAndFlush(
            new GraduationCertRule(admissionYear, 22, GraduationCertRuleType.BOTH_REQUIRED));

        GraduationDepartmentInfo userDept = departmentInfoRepository.saveAndFlush(
            new GraduationDepartmentInfo(
                admissionYear,
                22,
                "소프트웨어학과",
                deptCd,
                "소프트웨어융합대학",
                DeptGroup.SOFTWARE_CONVERGENCE_COLLEGE,
                EnglishTargetType.NON_MAJOR,
                CodingTargetType.EXEMPT,
                null
            )
        );

        User user = userRepository.saveAndFlush(UserFixture.singleMajorUser(admissionYear, userDept));

        englishCertCriterionRepository.saveAndFlush(
            EnglishCertCriterionFixture.createNonMajorEnglishCertCriterion(admissionYear)
        );
        classicCertCriterionRepository.saveAndFlush(
            ClassicCertCriterionFixture.createClassicCertCriterion(admissionYear)
        );

        // when
        GraduationCertCriteriaResponse response = graduationCertCriteriaService.getGraduationCertCriteria(user.getId());

        // then
        assertThat(response.certPolicy().graduationCertRuleType()).isEqualTo("BOTH_REQUIRED");
        assertThat(response.certPolicy().enableEnglish()).isTrue();
        assertThat(response.certPolicy().enableClassic()).isTrue();
        assertThat(response.certPolicy().enableCoding()).isFalse();
        assertThat(response.englishCertCriteria()).isNotNull();
        assertThat(response.classicCertCriteria()).isNotNull();
        assertThat(response.codingCertCriteria()).isNull();
    }

    @Test
    @DisplayName("18~22학번 단일 전공 영어 전공자인 경우 졸업인증 기준 데이터 조회를 검증한다.")
    void getGraduationCertCriteria_bothRequired_englishMajor() {
        // given
        int admissionYear = 2022;
        String deptCd = "2131";

        certRuleRepository.saveAndFlush(
            new GraduationCertRule(admissionYear, 22, GraduationCertRuleType.BOTH_REQUIRED));

        GraduationDepartmentInfo userDept = departmentInfoRepository.saveAndFlush(
            new GraduationDepartmentInfo(
                admissionYear,
                22,
                "영어영문학전공",
                deptCd,
                "인문과학대학",
                DeptGroup.LIBERAL_ARTS_COLLEGE,
                EnglishTargetType.ENGLISH_MAJOR,
                CodingTargetType.EXEMPT,
                null
            )
        );

        User user = userRepository.saveAndFlush(UserFixture.singleMajorUser(admissionYear, userDept));

        englishCertCriterionRepository.saveAndFlush(
            EnglishCertCriterionFixture.createMajorEnglishCertCriterion(admissionYear)
        );
        classicCertCriterionRepository.saveAndFlush(
            ClassicCertCriterionFixture.createClassicCertCriterion(admissionYear)
        );

        // when
        GraduationCertCriteriaResponse response = graduationCertCriteriaService.getGraduationCertCriteria(user.getId());

        // then
        assertThat(response.certPolicy().graduationCertRuleType()).isEqualTo("BOTH_REQUIRED");
        assertThat(response.certPolicy().enableEnglish()).isTrue();
        assertThat(response.certPolicy().enableClassic()).isTrue();
        assertThat(response.certPolicy().enableCoding()).isFalse();
        assertThat(response.englishCertCriteria()).isNotNull();
        assertThat(response.englishCertCriteria().englishTargetType()).isEqualTo("ENGLISH_MAJOR");
        assertThat(response.classicCertCriteria()).isNotNull();
        assertThat(response.codingCertCriteria()).isNull();
    }

    @Test
    @DisplayName("23~25학번 단일 전공 영어 비전공자/코딩 비전공자인 경우 졸업인증 기준 데이터 조회를 검증한다.")
    void getGraduationCertCriteria_twoOfThree_nonMajorEnglish_nonMajorCoding() {
        // given
        int admissionYear = 2025;
        String deptCd = "2658";

        certRuleRepository.saveAndFlush(new GraduationCertRule(admissionYear, 25, GraduationCertRuleType.TWO_OF_THREE));

        GraduationDepartmentInfo userDept = departmentInfoRepository.saveAndFlush(
            new GraduationDepartmentInfo(
                admissionYear,
                25,
                "수학통계학과",
                deptCd,
                "자연과학대학",
                DeptGroup.NATURAL_SCIENCES_COLLEGE,
                EnglishTargetType.NON_MAJOR,
                CodingTargetType.NON_MAJOR,
                null
            )
        );

        User user = userRepository.saveAndFlush(UserFixture.singleMajorUser(admissionYear, userDept));

        englishCertCriterionRepository.saveAndFlush(
            EnglishCertCriterionFixture.createNonMajorEnglishCertCriterion(admissionYear)
        );
        classicCertCriterionRepository.saveAndFlush(
            ClassicCertCriterionFixture.createClassicCertCriterion(admissionYear)
        );
        codingCertCriterionRepository.saveAndFlush(
            CodingCertCriterionFixture.createNonMajorCodingCertCriterion(admissionYear)
        );

        // when
        GraduationCertCriteriaResponse response = graduationCertCriteriaService.getGraduationCertCriteria(user.getId());

        // then
        assertThat(response.certPolicy().graduationCertRuleType()).isEqualTo("TWO_OF_THREE");
        assertThat(response.certPolicy().enableEnglish()).isTrue();
        assertThat(response.certPolicy().enableClassic()).isTrue();
        assertThat(response.certPolicy().enableCoding()).isTrue();
        assertThat(response.englishCertCriteria()).isNotNull();
        assertThat(response.classicCertCriteria()).isNotNull();
        assertThat(response.codingCertCriteria()).isNotNull();
    }

    @Test
    @DisplayName("23~25학번 단일 전공 영어 전공자/코딩 비전공자인 경우 졸업인증 기준 데이터 조회를 검증한다.")
    void getGraduationCertCriteria_twoOfThree_englishMajor_nonMajorCoding() {
        // given
        int admissionYear = 2025;
        String deptCd = "2135";

        certRuleRepository.saveAndFlush(new GraduationCertRule(admissionYear, 25, GraduationCertRuleType.TWO_OF_THREE));

        GraduationDepartmentInfo userDept = departmentInfoRepository.saveAndFlush(
            new GraduationDepartmentInfo(
                admissionYear,
                25,
                "영어데이터융합전공",
                deptCd,
                "인문과학대학",
                DeptGroup.LIBERAL_ARTS_COLLEGE,
                EnglishTargetType.ENGLISH_MAJOR,
                CodingTargetType.NON_MAJOR,
                null
            )
        );

        User user = userRepository.saveAndFlush(UserFixture.singleMajorUser(admissionYear, userDept));

        englishCertCriterionRepository.saveAndFlush(
            EnglishCertCriterionFixture.createMajorEnglishCertCriterion(admissionYear)
        );
        classicCertCriterionRepository.saveAndFlush(
            ClassicCertCriterionFixture.createClassicCertCriterion(admissionYear)
        );
        codingCertCriterionRepository.saveAndFlush(
            CodingCertCriterionFixture.createNonMajorCodingCertCriterion(admissionYear)
        );

        // when
        GraduationCertCriteriaResponse response = graduationCertCriteriaService.getGraduationCertCriteria(user.getId());

        // then
        assertThat(response.certPolicy().graduationCertRuleType()).isEqualTo("TWO_OF_THREE");
        assertThat(response.certPolicy().enableEnglish()).isTrue();
        assertThat(response.certPolicy().enableClassic()).isTrue();
        assertThat(response.certPolicy().enableCoding()).isTrue();
        assertThat(response.englishCertCriteria().englishTargetType()).isEqualTo("ENGLISH_MAJOR");
        assertThat(response.codingCertCriteria().codingTargetType()).isEqualTo("NON_MAJOR");
    }

    @Test
    @DisplayName("23~25학번 단일 전공 영어 비전공자/코딩 전공자인 경우 졸업인증 기준 데이터 조회를 검증한다.")
    void getGraduationCertCriteria_twoOfThree_nonMajorEnglish_codingMajor() {
        // given
        int admissionYear = 2025;
        String deptCd = "3523";

        certRuleRepository.saveAndFlush(new GraduationCertRule(admissionYear, 25, GraduationCertRuleType.TWO_OF_THREE));

        GraduationDepartmentInfo userDept = departmentInfoRepository.saveAndFlush(
            new GraduationDepartmentInfo(
                admissionYear, 25,
                "콘텐츠소프트웨어학과", deptCd,
                "인공지능융합대학",
                DeptGroup.AI_CONVERGENCE_COLLEGE,
                EnglishTargetType.NON_MAJOR,
                CodingTargetType.CODING_MAJOR,
                null
            )
        );

        User user = userRepository.saveAndFlush(UserFixture.singleMajorUser(admissionYear, userDept));

        englishCertCriterionRepository.saveAndFlush(
            EnglishCertCriterionFixture.createNonMajorEnglishCertCriterion(admissionYear)
        );
        classicCertCriterionRepository.saveAndFlush(
            ClassicCertCriterionFixture.createClassicCertCriterion(admissionYear)
        );
        codingCertCriterionRepository.saveAndFlush(
            CodingCertCriterionFixture.createMajorCodingCertCriterion(admissionYear)
        );

        // when
        GraduationCertCriteriaResponse response = graduationCertCriteriaService.getGraduationCertCriteria(user.getId());

        // then
        assertThat(response.certPolicy().graduationCertRuleType()).isEqualTo("TWO_OF_THREE");
        assertThat(response.certPolicy().enableEnglish()).isTrue();
        assertThat(response.certPolicy().enableClassic()).isTrue();
        assertThat(response.certPolicy().enableCoding()).isTrue();

        assertThat(response.codingCertCriteria().codingTargetType()).isEqualTo("CODING_MAJOR");
        assertThat(response.codingCertCriteria().altCourse().alt2CuriNo()).isNull();
        assertThat(response.codingCertCriteria().altCourse().alt2CuriNm()).isNull();
        assertThat(response.codingCertCriteria().altCourse().alt2MinGrade()).isNull();
    }

    @Test
    @DisplayName("23~25학번 복수 전공(영어 전공/코딩 전공)인 경우 졸업인증 기준 데이터 조회를 검증한다.")
    void getGraduationCertCriteria_twoOfThree_doubleMajor() {
        // given
        int admissionYear = 2025;

        // 정책
        certRuleRepository.saveAndFlush(new GraduationCertRule(admissionYear, 25, GraduationCertRuleType.TWO_OF_THREE));

        GraduationDepartmentInfo primaryuserDept = departmentInfoRepository.saveAndFlush(
            new GraduationDepartmentInfo(
                admissionYear, 25,
                "영어데이터융합전공", "2135",
                "인문과학대학",
                DeptGroup.LIBERAL_ARTS_COLLEGE,
                EnglishTargetType.ENGLISH_MAJOR,
                CodingTargetType.NON_MAJOR,
                null
            )
        );

        GraduationDepartmentInfo doubleuserDept = departmentInfoRepository.saveAndFlush(
            new GraduationDepartmentInfo(
                admissionYear, 25,
                "콘텐츠소프트웨어학과", "3523",
                "인공지능융합대학",
                DeptGroup.AI_CONVERGENCE_COLLEGE,
                EnglishTargetType.NON_MAJOR,
                CodingTargetType.CODING_MAJOR,
                null
            )
        );

        User user = userRepository.saveAndFlush(UserFixture.doubleMajorUser(admissionYear, primaryuserDept, doubleuserDept));

        englishCertCriterionRepository.saveAndFlush(
            EnglishCertCriterionFixture.createMajorEnglishCertCriterion(admissionYear)
        );
        classicCertCriterionRepository.saveAndFlush(
            ClassicCertCriterionFixture.createClassicCertCriterion(admissionYear)
        );
        codingCertCriterionRepository.saveAndFlush(
            CodingCertCriterionFixture.createNonMajorCodingCertCriterion(admissionYear)
        );

        // when
        GraduationCertCriteriaResponse response = graduationCertCriteriaService.getGraduationCertCriteria(user.getId());

        // then
        assertThat(response.certPolicy().graduationCertRuleType()).isEqualTo("TWO_OF_THREE");
        assertThat(response.certPolicy().enableEnglish()).isTrue();
        assertThat(response.certPolicy().enableClassic()).isTrue();
        assertThat(response.certPolicy().enableCoding()).isTrue();
        assertThat(response.criteriaTarget().englishTargetType()).isEqualTo("ENGLISH_MAJOR");
        assertThat(response.criteriaTarget().codingTargetType()).isEqualTo("NON_MAJOR");
        assertThat(response.englishCertCriteria().englishTargetType()).isEqualTo("ENGLISH_MAJOR");
        assertThat(response.codingCertCriteria().codingTargetType()).isEqualTo("NON_MAJOR");
    }

    @Test
    @DisplayName("단일 전공 영어 면제인 경우 영어 졸업인증 기준 데이터 조회를 검증한다.")
    void getGraduationCertCriteria_twoOfThree_englishExempt() {
        // given
        int admissionYear = 2022;
        String deptCd = "3220";

        certRuleRepository.saveAndFlush(
            new GraduationCertRule(admissionYear, 22, GraduationCertRuleType.TWO_OF_THREE)
        );

        GraduationDepartmentInfo userDept = departmentInfoRepository.saveAndFlush(
            new GraduationDepartmentInfo(
                admissionYear,
                22,
                "소프트웨어학과",
                deptCd,
                "소프트웨어융합대학",
                DeptGroup.SOFTWARE_CONVERGENCE_COLLEGE,
                EnglishTargetType.EXEMPT,
                CodingTargetType.NON_MAJOR,
                null
            )
        );

        User user = userRepository.saveAndFlush(UserFixture.singleMajorUser(admissionYear, userDept));

        classicCertCriterionRepository.saveAndFlush(
            ClassicCertCriterionFixture.createClassicCertCriterion(admissionYear)
        );
        codingCertCriterionRepository.saveAndFlush(
            CodingCertCriterionFixture.createNonMajorCodingCertCriterion(admissionYear)
        );

        // when
        GraduationCertCriteriaResponse response = graduationCertCriteriaService.getGraduationCertCriteria(user.getId());

        // then
        assertThat(response.certPolicy().graduationCertRuleType()).isEqualTo("TWO_OF_THREE");

        assertThat(response.certPolicy().enableEnglish()).isFalse();
        assertThat(response.certPolicy().enableClassic()).isTrue();
        assertThat(response.certPolicy().enableCoding()).isTrue();

        assertThat(response.criteriaTarget().englishTargetType()).isEqualTo("EXEMPT");
        assertThat(response.englishCertCriteria()).isNull();

        assertThat(response.classicCertCriteria()).isNotNull();
        assertThat(response.codingCertCriteria()).isNotNull();
    }

    @Test
    @DisplayName("단일 전공 코딩 면제인 경우 졸업인증 기준 데이터 조회를 검증한다.")
    void getGraduationCertCriteria_twoOfThree_codingExempt() {
        // given
        int admissionYear = 2022;
        String deptCd = "2131";

        certRuleRepository.saveAndFlush(
            new GraduationCertRule(admissionYear, 22, GraduationCertRuleType.TWO_OF_THREE)
        );

        GraduationDepartmentInfo userDept = departmentInfoRepository.saveAndFlush(
            new GraduationDepartmentInfo(
                admissionYear,
                22,
                "영어영문학전공",
                deptCd,
                "인문과학대학",
                DeptGroup.LIBERAL_ARTS_COLLEGE,
                EnglishTargetType.ENGLISH_MAJOR,
                CodingTargetType.EXEMPT,
                null
            )
        );

        User user = userRepository.saveAndFlush(UserFixture.singleMajorUser(admissionYear, userDept));

        englishCertCriterionRepository.saveAndFlush(
            EnglishCertCriterionFixture.createMajorEnglishCertCriterion(admissionYear)
        );
        classicCertCriterionRepository.saveAndFlush(
            ClassicCertCriterionFixture.createClassicCertCriterion(admissionYear)
        );

        // when
        GraduationCertCriteriaResponse response = graduationCertCriteriaService.getGraduationCertCriteria(user.getId());

        // then
        assertThat(response.certPolicy().graduationCertRuleType()).isEqualTo("TWO_OF_THREE");

        assertThat(response.certPolicy().enableEnglish()).isTrue();
        assertThat(response.certPolicy().enableClassic()).isTrue();
        assertThat(response.certPolicy().enableCoding()).isFalse();

        assertThat(response.criteriaTarget().codingTargetType()).isEqualTo("EXEMPT");
        assertThat(response.codingCertCriteria()).isNull();

        assertThat(response.englishCertCriteria()).isNotNull();
        assertThat(response.classicCertCriteria()).isNotNull();
    }

    @Test
    @DisplayName("단일 전공 영어/코딩 모두 면제인 경우 졸업인증 기준 데이터 조회를 검증한다.")
    void getGraduationCertCriteria_twoOfThree_englishAndCodingExempt() {
        // given
        int admissionYear = 2022;
        String deptCd = "2658";

        certRuleRepository.saveAndFlush(
            new GraduationCertRule(admissionYear, 22, GraduationCertRuleType.TWO_OF_THREE)
        );

        GraduationDepartmentInfo userDept = departmentInfoRepository.saveAndFlush(
            new GraduationDepartmentInfo(
                admissionYear,
                25,
                "수학통계학과",
                deptCd,
                "자연과학대학",
                DeptGroup.NATURAL_SCIENCES_COLLEGE,
                EnglishTargetType.EXEMPT,
                CodingTargetType.EXEMPT,
                null
            )
        );

        User user = userRepository.saveAndFlush(UserFixture.singleMajorUser(admissionYear, userDept));

        classicCertCriterionRepository.saveAndFlush(
            ClassicCertCriterionFixture.createClassicCertCriterion(admissionYear)
        );

        // when
        GraduationCertCriteriaResponse response = graduationCertCriteriaService.getGraduationCertCriteria(user.getId());

        // then
        assertThat(response.certPolicy().graduationCertRuleType()).isEqualTo("TWO_OF_THREE");

        assertThat(response.certPolicy().enableEnglish()).isFalse();
        assertThat(response.certPolicy().enableClassic()).isTrue();
        assertThat(response.certPolicy().enableCoding()).isFalse();

        assertThat(response.englishCertCriteria()).isNull();
        assertThat(response.codingCertCriteria()).isNull();
        assertThat(response.classicCertCriteria()).isNotNull();
    }

    @Test
    @DisplayName("학과 정보가 존재하지 않는 경우 예외를 검증한다.")
    void getGraduationCertCriteria_throwsDepartmentNotFound() {
        // given
        int admissionYear = 2025;
        String notExistDeptCd = "NOT_EXIST";

        certRuleRepository.saveAndFlush(new GraduationCertRule(admissionYear, 25, GraduationCertRuleType.TWO_OF_THREE));

        User user = userRepository.saveAndFlush(
            new User("00000000", "테스터", admissionYear, MajorType.SINGLE,
                "자연과학대학", "수학통계학과", notExistDeptCd,
                null, null, null
            )
        );

        // when & then
        assertThatThrownBy(() -> graduationCertCriteriaService.getGraduationCertCriteria(user.getId()))
            .isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.DEPARTMENT_NOT_FOUND.getMessage(), notExistDeptCd);
    }

    @Test
    @DisplayName("졸업인증 규칙이 존재하지 않는 경우 예외를 검증한다.")
    void getGraduationCertCriteria_throwsRuleNotFound() {
        // given
        int admissionYear = 2025;
        String deptCd = "2658";

        GraduationDepartmentInfo userDept = departmentInfoRepository.saveAndFlush(
            new GraduationDepartmentInfo(
                admissionYear,
                25,
                "수학통계학과", deptCd,
                "자연과학대학",
                DeptGroup.NATURAL_SCIENCES_COLLEGE,
                EnglishTargetType.NON_MAJOR,
                CodingTargetType.NON_MAJOR,
                null
            )
        );

        User user = userRepository.saveAndFlush(UserFixture.singleMajorUser(admissionYear, userDept));

        // when & then
        assertThatThrownBy(() -> graduationCertCriteriaService.getGraduationCertCriteria(user.getId()))
            .isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.GRADUATION_CERT_RULE_NOT_FOUND.getMessage(), admissionYear);
    }

    @Test
    @DisplayName("해당 입학년도 및 학과에 대한 영어인증 기준 데이터가 존재하지 않는 경우 예외를 검증한다.")
    void getGraduationCertCriteria_throwsEnglishCriteriaNotFound() {
        // given
        int admissionYear = 2025;
        String deptCd = "2658";

        certRuleRepository.saveAndFlush(new GraduationCertRule(admissionYear, 25, GraduationCertRuleType.TWO_OF_THREE));

        GraduationDepartmentInfo userDept = departmentInfoRepository.saveAndFlush(
            new GraduationDepartmentInfo(
                admissionYear,
                25,
                "수학통계학과",
                deptCd,
                "자연과학대학",
                DeptGroup.NATURAL_SCIENCES_COLLEGE,
                EnglishTargetType.NON_MAJOR,
                CodingTargetType.NON_MAJOR,
                null
            )
        );

        User user = userRepository.saveAndFlush(UserFixture.singleMajorUser(admissionYear, userDept));


        classicCertCriterionRepository.saveAndFlush(ClassicCertCriterionFixture.createClassicCertCriterion(admissionYear));
        codingCertCriterionRepository.saveAndFlush(CodingCertCriterionFixture.createNonMajorCodingCertCriterion(admissionYear));

        // when & then
        assertThatThrownBy(() -> graduationCertCriteriaService.getGraduationCertCriteria(user.getId()))
            .isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.ENGLISH_CERT_CRITERIA_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("해당 입학년도에 대한 고전인증 기준 데이터가 없을 경우 예외를 검증한다.")
    void getGraduationCertCriteria_throwsClassicCriteriaNotFound() {
        int admissionYear = 2025;

        certRuleRepository.saveAndFlush(new GraduationCertRule(admissionYear, 25, GraduationCertRuleType.TWO_OF_THREE));

        GraduationDepartmentInfo userDept = departmentInfoRepository.saveAndFlush(
            new GraduationDepartmentInfo(
                admissionYear,
                25,
                "수학통계학과",
                "2658",
                "자연과학대학",
                DeptGroup.NATURAL_SCIENCES_COLLEGE,
                EnglishTargetType.NON_MAJOR,
                CodingTargetType.NON_MAJOR,
                null
            )
        );

        User user = userRepository.saveAndFlush(UserFixture.singleMajorUser(admissionYear, userDept));

        englishCertCriterionRepository.saveAndFlush(
            EnglishCertCriterionFixture.createNonMajorEnglishCertCriterion(admissionYear)
        );
        codingCertCriterionRepository.saveAndFlush(
            CodingCertCriterionFixture.createNonMajorCodingCertCriterion(admissionYear)
        );

        assertThatThrownBy(() -> graduationCertCriteriaService.getGraduationCertCriteria(user.getId()))
            .isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.CLASSIC_CERT_CRITERIA_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("해당 입학년도 및 학과에 대한 코딩인증 기준 데이터가 없을 경우 예외를 검증한다.")
    void getGraduationCertCriteria_withoutCodingCriterion_throws() {
        int admissionYear = 2025;

        certRuleRepository.saveAndFlush(new GraduationCertRule(admissionYear, 25, GraduationCertRuleType.TWO_OF_THREE));

        GraduationDepartmentInfo userDept = departmentInfoRepository.saveAndFlush(
            new GraduationDepartmentInfo(
                admissionYear,
                25,
                "수학통계학과",
                "2658",
                "자연과학대학",
                DeptGroup.NATURAL_SCIENCES_COLLEGE,
                EnglishTargetType.NON_MAJOR,
                CodingTargetType.NON_MAJOR,
                null
            )
        );

        User user = userRepository.saveAndFlush(UserFixture.singleMajorUser(admissionYear, userDept));

        englishCertCriterionRepository.saveAndFlush(
            EnglishCertCriterionFixture.createNonMajorEnglishCertCriterion(admissionYear)
        );
        classicCertCriterionRepository.saveAndFlush(
            ClassicCertCriterionFixture.createClassicCertCriterion(admissionYear)
        );

        assertThatThrownBy(() -> graduationCertCriteriaService.getGraduationCertCriteria(user.getId()))
            .isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.CODING_CERT_CRITERIA_NOT_FOUND.getMessage());
    }
}
