package kr.allcll.backend.domain.basket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.allcll.backend.domain.basket.dto.BasketsEachSubject;
import kr.allcll.backend.domain.basket.dto.BasketsResponse;
import kr.allcll.backend.domain.basket.dto.SubjectBasketsResponse;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.subject.SubjectRepository;
import kr.allcll.backend.domain.subject.SubjectSpecifications;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.semester.Semester;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BasketService {

    private static final int NO_BASKET_TOTAL_COUNT = 0;

    private final BasketRepository basketRepository;
    private final SubjectRepository subjectRepository;

    public BasketsResponse findBasketsByCondition(
        String departmentCode,
        String professorName,
        String subjectName
    ) {
        Specification<Subject> condition = getCondition(departmentCode, professorName, subjectName, Semester.getCurrentSemester());
        List<Subject> subjects = subjectRepository.findAll(condition);
        List<BasketsEachSubject> result = getBasketsEachSubject(subjects);
        return new BasketsResponse(result);
    }

    private List<BasketsEachSubject> getBasketsEachSubject(List<Subject> subjects) {
        if (subjects.isEmpty()) {
            return List.of();
        }

        List<Long> subjectIds = subjects.stream()
            .map(Subject::getId)
            .toList();
        Map<Long, Integer> totalCountsBySubjectId = findTotalCountsBySubjectIds(subjectIds);

        return subjectIds.stream()
            .map(subjectId -> BasketsEachSubject.of(
                subjectId,
                totalCountsBySubjectId.getOrDefault(subjectId, NO_BASKET_TOTAL_COUNT)
            ))
            .toList();
    }

    private Map<Long, Integer> findTotalCountsBySubjectIds(List<Long> subjectIds) {
        Map<Long, Integer> totalCountsBySubjectId = new HashMap<>();
        basketRepository.findTotalCountsBySubjectIds(subjectIds, Semester.getCurrentSemester())
            .forEach(totalCount ->
                totalCountsBySubjectId.put(totalCount.subjectId(), totalCount.totalCount())
            );
        return totalCountsBySubjectId;
    }

    private Specification<Subject> getCondition(
        String departmentCode,
        String professorName,
        String subjectName,
        String semesterAt
    ) {
        return Specification.where(
            SubjectSpecifications.hasDepartmentCode(departmentCode)
                .and(SubjectSpecifications.hasProfessorName(professorName))
                .and(SubjectSpecifications.hasSubjectName(subjectName))
                .and(SubjectSpecifications.hasSemesterAt(semesterAt))
                .and(SubjectSpecifications.isNotDeleted())
        );
    }

    public SubjectBasketsResponse getEachSubjectBaskets(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.SUBJECT_NOT_FOUND, subjectId));
        List<Basket> baskets = getBaskets(subject);
        return SubjectBasketsResponse.from(subject.getEverytimeLectureId(), baskets);
    }

    private List<Basket> getBaskets(Subject subject) {
        return basketRepository.findBySubjectId(
                subject.getId(),
                Semester.getCurrentSemester()
            ).stream()
            .filter(Basket::isNotEmpty)
            .toList();
    }
}
