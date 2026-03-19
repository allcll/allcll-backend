package kr.allcll.backend.domain.graduation.check.cert;

import java.util.List;
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
    private final GraduationDepartmentInfoRepository graduationDepartmentInfoRepository;
    private final GraduationCheckCertResultRepository graduationCheckCertResultRepository;
    private final GraduationCodingCertResolver graduationCodingCertResolver;
    private final GraduationEnglishCertResolver graduationEnglishCertResolver;
    private final GraduationClassicsCertResolver graduationClassicsCertResolver;

    public GraduationCertInfo resolve(User user, OkHttpClient client) {
        GraduationDepartmentInfo userDept = graduationDepartmentInfoRepository
            .findByAdmissionYearAndDeptCd(user.getAdmissionYear(), user.getDeptCd())
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.DEPARTMENT_NOT_FOUND));

        List<CompletedCourse> completedCourses = completedCourseRepository.findAllByUserId(user.getId());

        GraduationCheckCertResult certResult = graduationCheckCertResultRepository.findByUserId(user.getId())
            .orElse(null);

        boolean englishPassed = graduationEnglishCertResolver.resolve(
            user,
            client,
            userDept,
            completedCourses,
            certResult
        );
        boolean codingPassed = graduationCodingCertResolver.resolve(
            user,
            client,
            userDept,
            completedCourses,
            certResult
        );
        ClassicsResult classicsResult = graduationClassicsCertResolver.resolve(user, client, certResult);

        return GraduationCertInfo.of(
            englishPassed,
            codingPassed,
            classicsResult.passed(),
            classicsResult.counts()
        );
    }
}
