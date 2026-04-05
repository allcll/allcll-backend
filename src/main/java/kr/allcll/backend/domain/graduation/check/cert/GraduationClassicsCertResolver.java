package kr.allcll.backend.domain.graduation.check.cert;

import kr.allcll.backend.domain.graduation.certification.ClassicAltCoursePolicy;
import kr.allcll.backend.domain.graduation.check.cert.dto.ClassicsCounts;
import kr.allcll.backend.domain.graduation.check.cert.dto.ClassicsResult;
import kr.allcll.backend.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraduationClassicsCertResolver {

    private final GraduationClassicsCertFetcher graduationClassicsCertFetcher;
    private final ClassicAltCoursePolicy classicAltCoursePolicy;

    public ClassicsResult resolve(
        User user,
        OkHttpClient client,
        GraduationCheckCertResult certResult
    ) {
        ClassicsCounts fallbackCounts = ClassicsCounts.fallback(certResult);

        if (certResult.isClassicsPassed()) {
            return ClassicsResult.passedWith(fallbackCounts);
        }

        ClassicsResult classicsResult = fetchClassicsResultFromExternal(client, fallbackCounts);
        boolean isSatisfiedByCrawledResult = classicsResult.isSatisfiedByCrawledResult();
        boolean satisfiedByAltCourse = classicAltCoursePolicy.isSatisfiedByAltCourse(user);
        if (isSatisfiedByCrawledResult || satisfiedByAltCourse) {
            return ClassicsResult.passedWith(classicsResult.counts());
        }

        return ClassicsResult.failedWith(classicsResult.counts());
    }

    private ClassicsResult fetchClassicsResultFromExternal(OkHttpClient client, ClassicsCounts fallbackCounts) {
        try {
            ClassicsResult classicsResult = graduationClassicsCertFetcher.fetchClassics(client);
            if (classicsResult == null) {
                log.warn("[졸업요건검사] 고전인증 결과가 null입니다. fallback 값 사용");
                return ClassicsResult.failedWith(fallbackCounts);
            }
            return classicsResult.withFallbackCounts(fallbackCounts);
        } catch (Exception e) {
            log.error("[졸업요건검사] 고전인증 여부를 불러오지 못했습니다.", e);
            return ClassicsResult.failedWith(fallbackCounts);
        }
    }
}
