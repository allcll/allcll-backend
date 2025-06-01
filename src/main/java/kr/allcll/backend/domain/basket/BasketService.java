package kr.allcll.backend.domain.basket;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
        Specification<Subject> condition = getCondition(departmentCode, professorName, subjectName);
        List<Subject> subjects = subjectRepository.findAll(condition);
        List<BasketsEachSubject> result = getBasketsEachSubject(subjects);
        return new BasketsResponse(result);
    }

    private List<BasketsEachSubject> getBasketsEachSubject(List<Subject> subjects) {
        List<BasketsEachSubject> result = new ArrayList<>();
        for (Subject subject : subjects) {
            List<Basket> baskets = basketRepository.findBySubjectId(
                subject.getId(),
                Semester.getCodeValue(LocalDate.now())
            );
            result.add(BasketsEachSubject.from(subject, baskets));
        }
        return result;
    }

    private Specification<Subject> getCondition(
        String departmentCode,
        String professorName,
        String subjectName
    ) {
        return Specification.where(
            SubjectSpecifications.hasDepartmentCode(departmentCode)
                .and(SubjectSpecifications.hasProfessorName(professorName))
                .and(SubjectSpecifications.hasSubjectName(subjectName))
        );
    }

    public SubjectBasketsResponse getEachSubjectBaskets(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.SUBJECT_NOT_FOUND));
        List<Basket> baskets = getBaskets(subject);
        return SubjectBasketsResponse.from(subject.getEverytimeLectureId(), baskets);
    }

    private List<Basket> getBaskets(Subject subject) {
        return basketRepository.findBySubjectId(
                subject.getId(),
                Semester.getCodeValue(LocalDate.now())
            ).stream()
            .filter(Basket::isNotEmpty)
            .toList();
    }
}
