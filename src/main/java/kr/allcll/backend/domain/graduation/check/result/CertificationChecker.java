package kr.allcll.backend.domain.graduation.check.result;

import java.util.List;
import java.util.Optional;
import kr.allcll.backend.domain.graduation.certification.EnglishCertCriterion;
import kr.allcll.backend.domain.graduation.certification.EnglishCertCriterionRepository;
import kr.allcll.backend.domain.graduation.certification.EnglishTargetType;
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

@Component
@RequiredArgsConstructor
public class CertificationChecker {

    private final UserRepository userRepository;
    private final EnglishCertCriterionRepository englishCertCriterionRepository;
    private final GraduationDepartmentInfoRepository graduationDepartmentInfoRepository;
    private final GraduationCheckCertResultRepository graduationCheckCertResultRepository;

    public CertResult checkAndUpdate(Long userId, List<CompletedCourseDto> completedCourses) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.USER_NOT_FOUND));
        GraduationCheckCertResult certResult = graduationCheckCertResultRepository.findByUserId(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.GRADUATION_CERT_NOT_FOUND));
        if (Boolean.TRUE.equals(certResult.getIsEnglishCertPassed())) {
            return CertResult.from(certResult);
        }
        if (isEnglishAltCourseCompleted(user, completedCourses)) {
            certResult.updateEnglishPassedByAltCourse();
        }
        return CertResult.from(certResult);
    }

    private boolean isEnglishAltCourseCompleted(User user, List<CompletedCourseDto> completedCourses) {
        GraduationDepartmentInfo graduationDepartmentInfo = graduationDepartmentInfoRepository
            .findByAdmissionYearAndDeptCd(user.getAdmissionYear(), user.getDeptCd())
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.DEPARTMENT_NOT_FOUND));
        EnglishTargetType englishTargetType = graduationDepartmentInfo.getEnglishTargetType();

        Optional<EnglishCertCriterion> englishCertCriterionOpt =
            englishCertCriterionRepository.findByAdmissionYearAndEnglishTargetType(
                user.getAdmissionYear(),
                englishTargetType
            );

        if (englishCertCriterionOpt.isEmpty()) {
            return false;
        }

        EnglishCertCriterion englishCertCriterion = englishCertCriterionOpt.get();
        String altCuriNo = englishCertCriterion.getAltCuriNo();
        for (CompletedCourseDto completedCourse : completedCourses) {
            if (!completedCourse.isCreditEarned()) {
                continue;
            }
            if (englishCertCriterion.matchesAltCourse(altCuriNo)) {
                return true;
            }
        }
        return false;
    }
}
