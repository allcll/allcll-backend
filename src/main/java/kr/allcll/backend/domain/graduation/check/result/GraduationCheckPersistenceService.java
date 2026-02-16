package kr.allcll.backend.domain.graduation.check.result;

import java.util.List;
import kr.allcll.backend.domain.graduation.check.result.dto.CheckResult;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCategory;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GraduationCheckPersistenceService {

    private final GraduationCheckRepository graduationCheckRepository;
    private final GraduationCheckCategoryResultRepository graduationCheckCategoryResultRepository;
    private final GraduationCheckBalanceAreaResultRepository graduationCheckBalanceAreaResultRepository;

    public void saveCheckResult(Long userId, CheckResult checkResult) {
        saveOrUpdateGraduationCheck(userId, checkResult);
        saveOrUpdateCategoryResults(userId, checkResult.categories());
        saveBalanceAreaResults(userId, checkResult.categories());
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

    private void saveBalanceAreaResults(Long userId, List<GraduationCategory> categories) {
        GraduationCategory balanceCategory = categories.stream()
            .filter(c -> c.categoryType() == CategoryType.BALANCE_REQUIRED)
            .findFirst()
            .orElse(null);

        if (balanceCategory == null || balanceCategory.earnedAreas() == null) {
            return;
        }

        graduationCheckBalanceAreaResultRepository.deleteAllByUserId(userId);
        graduationCheckBalanceAreaResultRepository.flush();

        List<GraduationCheckBalanceAreaResult> areaResults = balanceCategory.earnedAreas().stream()
            .map(area -> new GraduationCheckBalanceAreaResult(userId, area))
            .toList();

        graduationCheckBalanceAreaResultRepository.saveAll(areaResults);
    }
}
