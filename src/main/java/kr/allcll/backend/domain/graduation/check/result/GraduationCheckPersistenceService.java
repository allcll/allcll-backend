package kr.allcll.backend.domain.graduation.check.result;

import java.util.List;
import kr.allcll.backend.domain.graduation.check.result.dto.CheckResult;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GraduationCheckPersistenceService {

    private final GraduationCheckRepository graduationCheckRepository;
    private final GraduationCheckCategoryResultRepository graduationCheckCategoryResultRepository;

    public void saveCheckResult(Long userId, CheckResult checkResult) {
        saveOrUpdateGraduationCheck(userId, checkResult);
        saveOrUpdateCategoryResults(userId, checkResult.categories());
    }

    private void saveOrUpdateGraduationCheck(Long userId, CheckResult checkResult) {
        GraduationCheck graduationCheck = graduationCheckRepository.findByUserId(userId)
            .orElse(null);

        if (graduationCheck != null) {
            graduationCheck.update(
                checkResult.isGraduatable(),
                checkResult.totalCredits(),
                checkResult.requiredTotalCredits(),
                checkResult.remainingCredits()
            );
            return;
        }
        graduationCheck = new GraduationCheck(
            userId,
            checkResult.isGraduatable(),
            checkResult.totalCredits(),
            checkResult.requiredTotalCredits(),
            checkResult.remainingCredits()
        );
        graduationCheckRepository.save(graduationCheck);
    }

    private void saveOrUpdateCategoryResults(Long userId, List<GraduationCategory> categories) {
        List<GraduationCheckCategoryResult> existingCategoryResults = graduationCheckCategoryResultRepository.findAllByUserId(
            userId);
        graduationCheckCategoryResultRepository.deleteAllInBatch(existingCategoryResults);
        graduationCheckCategoryResultRepository.flush();

        List<GraduationCheckCategoryResult> newCategoryResults = categories.stream()
            .map(category ->
                new GraduationCheckCategoryResult(
                    userId,
                    category.majorScope(),
                    category.categoryType(),
                    category.earnedCredits(),
                    category.requiredCredits(),
                    category.remainingCredits(),
                    category.satisfied()
                )
            ).toList();

        graduationCheckCategoryResultRepository.saveAll(newCategoryResults);
    }
}
