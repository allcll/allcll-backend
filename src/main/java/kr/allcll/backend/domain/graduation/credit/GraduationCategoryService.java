package kr.allcll.backend.domain.graduation.credit;

import java.util.ArrayList;
import java.util.List;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseRepository;
import kr.allcll.backend.domain.graduation.credit.dto.GraduationCategoriesResponse;
import kr.allcll.backend.domain.graduation.credit.dto.GraduationCategoryResponse;
import kr.allcll.backend.domain.graduation.credit.dto.GraduationContextResponse;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GraduationCategoryService {

    private final UserRepository userRepository;
    private final MajorCategoryResolver majorCategoryResolver;
    private final UncompletedCourseFilter uncompletedCourseFilter;
    private final NonMajorCategoryResolver nonMajorCategoryResolver;
    private final CompletedCourseRepository completedCourseRepository;

    public GraduationCategoriesResponse getAllCategories(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.USER_NOT_FOUND));

        GraduationContextResponse graduationContextResponse = GraduationContextResponse.of(
            user.getAdmissionYear(),
            user.getMajorType(),
            user.getDeptCd(),
            user.getDeptNm(),
            user.getDoubleDeptCd(),
            user.getDoubleDeptNm()
        );

        List<GraduationCategoryResponse> categories = new ArrayList<>();
        addNonMajorCategories(categories, user);
        addMajorCategories(categories, user);

        List<GraduationCategoryResponse> filteredCourses = filterUncompletedCourses(userId, user, categories);

        return GraduationCategoriesResponse.of(graduationContextResponse, filteredCourses);
    }

    private void addNonMajorCategories(List<GraduationCategoryResponse> graduationCategoryResponses, User user) {
        List<GraduationCategoryResponse> nonMajorCategories =
            nonMajorCategoryResolver.resolve(user.getAdmissionYear(), user.getDeptCd());
        graduationCategoryResponses.addAll(nonMajorCategories);
    }

    private void addMajorCategories(List<GraduationCategoryResponse> graduationCategoryResponses, User user) {
        List<GraduationCategoryResponse> majorCategories = majorCategoryResolver.resolve(
            user.getAdmissionYear(),
            user.getMajorType(),
            user.getDeptCd(),
            user.getDoubleDeptCd(),
            user
        );
        graduationCategoryResponses.addAll(majorCategories);
    }

    private List<GraduationCategoryResponse> filterUncompletedCourses(
        Long userId,
        User user,
        List<GraduationCategoryResponse> categories
    ) {
        List<CompletedCourse> earnedCourses = completedCourseRepository.findEarnedCourses(userId);
        return uncompletedCourseFilter.filterUncompletedCourses(
            user.getAdmissionYear(),
            categories,
            earnedCourses
        );
    }
}
