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
public class GraduationEnglishCertResolver {

    private final GraduationEnglishCertFetcher graduationEnglishCertFetcher;
    private final GraduationCertificationAltCoursePolicy englishAltCoursePolicy;

    public boolean resolve(
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

    private boolean isEnglishAlreadyPassed(GraduationCheckCertResult certResult) {
        return Boolean.TRUE.equals(certResult.getIsEnglishCertPassed());
    }
}
