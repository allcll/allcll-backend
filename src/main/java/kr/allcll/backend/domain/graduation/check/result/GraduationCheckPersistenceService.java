package kr.allcll.backend.domain.graduation.check.result;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredArea;
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
        saveOrUpdateBalanceAreaResults(userId, checkResult.categories());
    }

    private void saveOrUpdateGraduationCheck(Long userId, CheckResult checkResult) {
        GraduationCheck existingCheck = graduationCheckRepository.findByUserId(userId)
            .orElse(null);
        if (existingCheck != null) {
            existingCheck.update(
                checkResult.isGraduatable(),
                checkResult.totalCredits(),
                checkResult.requiredTotalCredits(),
                checkResult.remainingCredits()
            );
            return;
        }

        GraduationCheck newCheck = new GraduationCheck(
            userId,
            checkResult.isGraduatable(),
            checkResult.totalCredits(),
            checkResult.requiredTotalCredits(),
            checkResult.remainingCredits()
        );
        graduationCheckRepository.save(newCheck);
    }

    private void saveOrUpdateCategoryResults(Long userId, List<GraduationCategory> categories) {
        Map<String, GraduationCheckCategoryResult> existingResultsByKey =
            graduationCheckCategoryResultRepository.findAllByUserId(userId)
                .stream()
                .collect(Collectors.toMap(
                    result -> generateCategoryKey(result.getMajorScope(), result.getCategoryType()),
                    Function.identity()
                ));

        Set<String> newCategoryKeys = categories.stream()
            .map(category -> generateCategoryKey(category.majorScope(), category.categoryType()))
            .collect(Collectors.toSet());

        // 3. 기존 데이터 중 새 계산 결과에 없는 것 삭제
        List<GraduationCheckCategoryResult> deleteTargets =
            existingResultsByKey.entrySet().stream()
                .filter(entry -> !newCategoryKeys.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
        if (!deleteTargets.isEmpty()) {
            graduationCheckCategoryResultRepository.deleteAllInBatch(deleteTargets);
        }

        for (GraduationCategory category : categories) {
            String key = generateCategoryKey(category.majorScope(), category.categoryType());
            GraduationCheckCategoryResult existingResult = existingResultsByKey.get(key);

            if (existingResult != null) {
                existingResult.update(
                    category.earnedCredits(),
                    category.requiredCredits(),
                    category.remainingCredits(),
                    category.satisfied()
                );
                continue;
            }

            GraduationCheckCategoryResult newResult = new GraduationCheckCategoryResult(
                userId,
                category.majorScope(),
                category.categoryType(),
                category.earnedCredits(),
                category.requiredCredits(),
                category.remainingCredits(),
                category.satisfied()
            );
            graduationCheckCategoryResultRepository.save(newResult);
        }
    }

    private String generateCategoryKey(MajorScope scope, CategoryType type) {
        return scope + ":" + type;
    }

    private void saveOrUpdateBalanceAreaResults(Long userId, List<GraduationCategory> categories) {
        GraduationCategory balanceCategory = categories.stream()
            .filter(category -> category.categoryType() == CategoryType.BALANCE_REQUIRED)
            .findFirst()
            .orElse(null);
        if (balanceCategory == null) {
            return;
        }

        Set<BalanceRequiredArea> newAreas = balanceCategory.earnedAreas();
        if (newAreas == null || newAreas.isEmpty()) {
            return;
        }

        Map<BalanceRequiredArea, GraduationCheckBalanceAreaResult> existingResultsByArea =
            graduationCheckBalanceAreaResultRepository.findAllByUserId(userId)
                .stream()
                .collect(Collectors.toMap(
                    GraduationCheckBalanceAreaResult::getArea,
                    Function.identity()
                ));

        List<GraduationCheckBalanceAreaResult> deleteTargets =
            existingResultsByArea.entrySet().stream()
                .filter(entry -> !newAreas.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
        if (!deleteTargets.isEmpty()) {
            graduationCheckBalanceAreaResultRepository.deleteAllInBatch(deleteTargets);
        }

        List<GraduationCheckBalanceAreaResult> createTargets =
            newAreas.stream()
                .filter(area -> !existingResultsByArea.containsKey(area))
                .map(area -> new GraduationCheckBalanceAreaResult(userId, area))
                .toList();
        if (!createTargets.isEmpty()) {
            graduationCheckBalanceAreaResultRepository.saveAll(createTargets);
        }
    }
}
