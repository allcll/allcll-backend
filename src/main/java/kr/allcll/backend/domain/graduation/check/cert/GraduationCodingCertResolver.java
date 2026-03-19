package kr.allcll.backend.domain.graduation.check.cert;

import java.util.List;
import kr.allcll.backend.domain.graduation.certification.GraduationCertificationAltCoursePolicy;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraduationCodingCertResolver {

    private final GraduationCodingCertFetcher graduationCodingCertFetcher;
    private final GraduationCertificationAltCoursePolicy codingAltCoursePolicy;

    public boolean resolve(
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

    private boolean isCodingAlreadyPassed(GraduationCheckCertResult certResult) {
        if (certResult == null) {
            return false;
        }
        return Boolean.TRUE.equals(certResult.getIsCodingCertPassed());
    }
}
