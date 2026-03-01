package kr.allcll.backend.domain.graduation.check.cert;

import java.util.List;
import kr.allcll.backend.domain.graduation.certification.GraduationCertificationAltCoursePolicy;
import kr.allcll.backend.domain.graduation.check.cert.dto.ClassicsCounts;
import kr.allcll.backend.domain.graduation.check.cert.dto.ClassicsResult;
import kr.allcll.backend.domain.graduation.check.cert.dto.GraduationCertInfo;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseRepository;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfoRepository;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraduationCertResolver {

    private final CompletedCourseRepository completedCourseRepository;
    private final GraduationCodingCertFetcher graduationCodingCertFetcher;
    private final GraduationEnglishCertFetcher graduationEnglishCertFetcher;
    private final GraduationClassicsCertFetcher graduationClassicsCertFetcher;
    private final GraduationCertificationAltCoursePolicy codingAltCoursePolicy;
    private final GraduationCertificationAltCoursePolicy englishAltCoursePolicy;
    private final GraduationDepartmentInfoRepository graduationDepartmentInfoRepository;
    private final GraduationCheckCertResultRepository graduationCheckCertResultRepository;

    public GraduationCertInfo resolve(User user, OkHttpClient client) {
        GraduationDepartmentInfo userDept = graduationDepartmentInfoRepository
            .findByAdmissionYearAndDeptCd(user.getAdmissionYear(), user.getDeptCd())
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.DEPARTMENT_NOT_FOUND));

        List<CompletedCourse> completedCourses = completedCourseRepository.findAllByUserId(user.getId());

        GraduationCheckCertResult certResult = graduationCheckCertResultRepository.findByUserId(user.getId())
            .orElse(null);

        boolean englishPassed = resolveEnglishPassed(user, client, userDept, completedCourses, certResult);
        boolean codingPassed = resolveCodingPassed(user, client, userDept, completedCourses, certResult);
        ClassicsResult classicsResult = resolveClassics(client, certResult);

        return GraduationCertInfo.of(
            englishPassed,
            codingPassed,
            classicsResult.passed(),
            classicsResult.counts()
        );
    }

    private boolean resolveEnglishPassed(
        User user,
        OkHttpClient client,
        GraduationDepartmentInfo userDept,
        List<CompletedCourse> completedCourses,
        GraduationCheckCertResult certResult
    ) {
        if (isEnglishAlreadyPassed(certResult)) {
            return true;
        }

        if (englishAltCoursePolicy.isSatisfiedByAltCourse(user, userDept, completedCourses)) {
            return true;
        }

        try {
            return graduationEnglishCertFetcher.fetchEnglishPass(client);
        } catch (Exception e) {
            log.error("[졸업요건검사] 영어인증 여부를 불러오지 못했습니다.", e);
            return false;
        }
    }

    private boolean resolveCodingPassed(
        User user,
        OkHttpClient client,
        GraduationDepartmentInfo userDept,
        List<CompletedCourse> completedCourses,
        GraduationCheckCertResult certResult
    ) {
        if (isCodingAlreadyPassed(certResult)) {
            return true;
        }

        if (codingAltCoursePolicy.isSatisfiedByAltCourse(user, userDept, completedCourses)) {
            return true;
        }

        try {
            return graduationCodingCertFetcher.fetchCodingPass(client);
        } catch (Exception e) {
            log.error("[졸업요건검사] 코딩인증 정보를 불러오지 못했습니다.", e);
            return false;
        }
    }

    private ClassicsResult resolveClassics(
        OkHttpClient client,
        GraduationCheckCertResult certResult
    ) {
        ClassicsCounts fallbackCounts = ClassicsCounts.fallback(certResult);

        if (isClassicsAlreadyPassed(certResult)) {
            return ClassicsResult.passedWith(fallbackCounts);
        }

        try {
            ClassicsResult classicsResult = graduationClassicsCertFetcher.fetchClassics(client);
            if (classicsResult == null) {
                return ClassicsResult.empty();
            }
            return classicsResult.withFallbackCounts(fallbackCounts);
        } catch (Exception e) {
            log.error("[졸업요건검사] 고전인증 여부를 불러오지 못했습니다.", e);
            return ClassicsResult.failedWith(fallbackCounts);
        }
    }

    private boolean isEnglishAlreadyPassed(GraduationCheckCertResult certResult) {
        if (certResult == null) {
            return false;
        }
        return Boolean.TRUE.equals(certResult.getIsEnglishCertPassed());
    }

    private boolean isCodingAlreadyPassed(GraduationCheckCertResult certResult) {
        if (certResult == null) {
            return false;
        }
        return Boolean.TRUE.equals(certResult.getIsCodingCertPassed());
    }

    private boolean isClassicsAlreadyPassed(GraduationCheckCertResult certResult) {
        if (certResult == null) {
            return false;
        }
        return Boolean.TRUE.equals(certResult.getIsClassicsCertPassed());
    }
}
