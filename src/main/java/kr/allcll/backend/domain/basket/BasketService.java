package kr.allcll.backend.domain.basket;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

@Service
@RequiredArgsConstructor
public class BasketService {

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

    // #1 N+1 제거: 과목별 개별 쿼리 → 한 번의 배치 쿼리로 변경
    private List<BasketsEachSubject> getBasketsEachSubject(List<Subject> subjects) {
        if (subjects.isEmpty()) {
            return List.of();
        }
        List<Long> subjectIds = subjects.stream().map(Subject::getId).toList();
        List<Basket> allBaskets = basketRepository.findBySubjectIds(subjectIds, Semester.getCurrentSemester());
        Map<Long, List<Basket>> basketsBySubjectId = allBaskets.stream()
            .collect(Collectors.groupingBy(basket -> basket.getSubject().getId()));

        return subjects.stream()
            .map(subject -> BasketsEachSubject.from(
                subject,
                basketsBySubjectId.getOrDefault(subject.getId(), Collections.emptyList())
            ))
            .toList();
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
