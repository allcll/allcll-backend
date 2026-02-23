package kr.allcll.backend.domain.graduation.check.result;

import java.util.List;
import kr.allcll.backend.domain.graduation.certification.GraduationCertificationAltCoursePolicy;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResultRepository;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
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
@RequiredArgsConstructor
public class CertificationChecker {

    private final UserRepository userRepository;
    private final GraduationCertificationAltCoursePolicy codingAltCoursePolicy;
    private final GraduationCertificationAltCoursePolicy englishAltCoursePolicy;
    private final GraduationDepartmentInfoRepository graduationDepartmentInfoRepository;
    private final GraduationCheckCertResultRepository graduationCheckCertResultRepository;

    @Transactional
    public CertResult checkAndUpdate(Long userId, List<CompletedCourseDto> completedCourses) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.USER_NOT_FOUND));
        GraduationDepartmentInfo departmentInfo = graduationDepartmentInfoRepository
            .findByAdmissionYearAndDeptCd(user.getAdmissionYear(), user.getDeptCd())
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.DEPARTMENT_NOT_FOUND));
        GraduationCheckCertResult certResult = graduationCheckCertResultRepository.findByUserId(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.GRADUATION_CERT_NOT_FOUND));

        boolean isChanged = false;
        if (englishAltCoursePolicy.isSatisfiedByAltCourse(user, departmentInfo, completedCourses, certResult)) {
            certResult.passEnglish();
            isChanged = true;
        }
        if (codingAltCoursePolicy.isSatisfiedByAltCourse(user, departmentInfo, completedCourses, certResult)) {
            certResult.passCoding();
            isChanged = true;
        }
        if (isChanged) {
            certResult.reCalculate();
        }

        return CertResult.from(certResult);
    }
}
