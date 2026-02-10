package kr.allcll.backend.domain.graduation.check.result;

import java.util.List;
import java.util.Optional;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GraduationCheckCategoryResultRepository extends JpaRepository<GraduationCheckCategoryResult, Long> {

    List<GraduationCheckCategoryResult> findAllByUserId(Long userId);

    Optional<GraduationCheckCategoryResult> findByUserIdAndMajorScopeAndCategoryType(
        Long userId,
        MajorScope majorScope,
        CategoryType categoryType
    );

    void deleteAllByUserId(Long userId);
}
