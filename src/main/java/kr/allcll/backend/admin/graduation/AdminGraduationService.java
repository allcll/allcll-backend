package kr.allcll.backend.admin.graduation;

import kr.allcll.backend.admin.graduation.dto.GraduationDetailResponse;
import kr.allcll.backend.admin.graduation.dto.GraduationUserResponse;
import kr.allcll.backend.domain.graduation.certification.GraduationCertCriteriaService;
import kr.allcll.backend.domain.graduation.certification.dto.GraduationCertCriteriaResponse;
import kr.allcll.backend.domain.graduation.check.result.GraduationCheckService;
import kr.allcll.backend.domain.graduation.check.result.dto.CompletedCoursesResponse;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCheckResponse;
import kr.allcll.backend.domain.graduation.credit.GraduationCategoryService;
import kr.allcll.backend.domain.graduation.credit.dto.GraduationCategoriesResponse;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminGraduationService {

    private final UserRepository userRepository;
    private final GraduationCheckService graduationCheckService;
    private final GraduationCategoryService graduationCategoryService;
    private final GraduationCertCriteriaService graduationCertCriteriaService;

    public GraduationDetailResponse getGraduationDetail(String studentId) {
        User user = userRepository.findByStudentId(studentId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.USER_NOT_FOUND));
        Long userId = user.getId();

        GraduationUserResponse userInfo = GraduationUserResponse.from(user);
        GraduationCheckResponse checkData = graduationCheckService.getCheckResult(userId);
        CompletedCoursesResponse courses = graduationCheckService.getAllCompletedCourses(userId);
        GraduationCategoriesResponse criteriaCategories = graduationCategoryService.getAllCategories(userId);
        GraduationCertCriteriaResponse certCriteria = graduationCertCriteriaService.getGraduationCertCriteria(userId);

        return new GraduationDetailResponse(
            userInfo,
            checkData,
            courses,
            criteriaCategories,
            certCriteria
        );
    }
}
