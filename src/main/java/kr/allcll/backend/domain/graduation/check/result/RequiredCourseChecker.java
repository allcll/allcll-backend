package kr.allcll.backend.domain.graduation.check.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
import kr.allcll.backend.domain.graduation.check.result.dto.MissingCourse;
import kr.allcll.backend.domain.graduation.check.result.dto.RequiredCourseRecommendation;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.domain.graduation.credit.RequiredCourse;
import kr.allcll.backend.domain.graduation.credit.RequiredCourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequiredCourseChecker {

    private final RequiredCourseRepository requiredCourseRepository;

    public List<RequiredCourseRecommendation> check(
        Integer admissionYear,
        String deptNm,
        List<CompletedCourseDto> completedCourses
    ) {
        // 필수 과목 목록 조회
        List<RequiredCourse> requiredCourses = requiredCourseRepository
            .findByAdmissionYearAndDeptNmAndRequired(admissionYear, deptNm, true);

        // 이수한 과목 번호 Set
        Set<String> completedCuriNos = completedCourses.stream()
            .map(CompletedCourseDto::curiNo)
            .collect(Collectors.toSet());

        // 미이수 필수 과목 찾기
        Map<CategoryType, List<MissingCourse>> missingByCategory = new HashMap<>();

        // altGroup별로 그룹화
        Map<String, List<RequiredCourse>> groupedByAltGroup = requiredCourses.stream()
            .filter(rc -> rc.getAltGroup() != null)
            .collect(Collectors.groupingBy(RequiredCourse::getAltGroup));

        // altGroup이 있는 과목 처리 (선택 과목)
        for (Map.Entry<String, List<RequiredCourse>> entry : groupedByAltGroup.entrySet()) {
            List<RequiredCourse> groupCourses = entry.getValue();

            // 그룹 내 과목 중 하나라도 이수했는지 확인
            boolean anyCompleted = groupCourses.stream()
                .anyMatch(rc -> completedCuriNos.contains(rc.getCuriNo()));

            if (!anyCompleted) {
                // 그룹 내 모든 과목을 미이수로 표시
                for (RequiredCourse rc : groupCourses) {
                    missingByCategory
                        .computeIfAbsent(rc.getCategoryType(), k -> new ArrayList<>())
                        .add(new MissingCourse(
                            rc.getCuriNo(),
                            rc.getCuriNm(),
                            rc.getAltGroup()
                        ));
                }
            }
        }

        // altGroup이 없는 과목 처리 (단일 필수 과목)
        requiredCourses.stream()
            .filter(rc -> rc.getAltGroup() == null)
            .filter(rc -> !completedCuriNos.contains(rc.getCuriNo()))
            .forEach(rc -> missingByCategory
                .computeIfAbsent(rc.getCategoryType(), k -> new ArrayList<>())
                .add(new MissingCourse(
                    rc.getCuriNo(),
                    rc.getCuriNm(),
                    null
                )));

        // CategoryType별로 RecommendationResult 생성
        return missingByCategory.entrySet().stream()
            .map(entry -> new RequiredCourseRecommendation(
                MajorScope.PRIMARY, // TODO: 복수전공 처리
                entry.getKey(),
                entry.getValue()
            ))
            .toList();
    }
}
