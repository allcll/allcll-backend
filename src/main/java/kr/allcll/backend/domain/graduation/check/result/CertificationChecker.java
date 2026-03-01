package kr.allcll.backend.domain.graduation.check.result;

import java.util.List;
import kr.allcll.backend.domain.graduation.certification.GraduationCertificationAltCoursePolicy;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResultRepository;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.check.result.dto.CertResult;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfoRepository;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CertificationChecker {

    private final UserRepository userRepository;
    private final GraduationCertificationAltCoursePolicy codingAltCoursePolicy;
    private final GraduationCertificationAltCoursePolicy englishAltCoursePolicy;
    private final GraduationDepartmentInfoRepository graduationDepartmentInfoRepository;
    private final GraduationCheckCertResultRepository graduationCheckCertResultRepository;

    @Transactional
    public CertResult checkAndUpdate(Long userId, List<CompletedCourse> earnedCourses) {
        applyAltCourse(userId, earnedCourses);
        GraduationCheckCertResult certResult = graduationCheckCertResultRepository.findByUserId(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.GRADUATION_CERT_NOT_FOUND));
        return CertResult.from(certResult);
    }

    private void applyAltCourse(Long userId, List<CompletedCourse> earnedCourses) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.USER_NOT_FOUND));

        GraduationDepartmentInfo userDept = graduationDepartmentInfoRepository
            .findByAdmissionYearAndDeptCd(user.getAdmissionYear(), user.getDeptCd())
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.DEPARTMENT_NOT_FOUND));

        GraduationCheckCertResult certResult = graduationCheckCertResultRepository.findByUserId(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.GRADUATION_CERT_NOT_FOUND));

        boolean isChanged = false;
        if (englishAltCoursePolicy.isSatisfiedByAltCourse(user, userDept, earnedCourses)) {
            certResult.passEnglish();
            isChanged = true;
        }
        if (codingAltCoursePolicy.isSatisfiedByAltCourse(user, userDept, earnedCourses)) {
            certResult.passCoding();
            isChanged = true;
        }
        if (isChanged) {
            certResult.reCalculate();
        }
    }
}
