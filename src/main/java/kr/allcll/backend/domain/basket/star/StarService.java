package kr.allcll.backend.domain.basket.star;

import java.util.List;
import kr.allcll.backend.domain.basket.star.dto.StarredSubjectIdResponse;
import kr.allcll.backend.domain.basket.star.dto.StarredSubjectIdsResponse;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.subject.SubjectRepository;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.semester.Semester;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StarService {

    private static final int MAX_STAR_NUMBER = 50;

    private final StarRepository starRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public void addStarOnSubject(Long subjectId, String token) {

        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.SUBJECT_NOT_FOUND, subjectId));
        validateCanAddStar(subject, token);
        starRepository.save(new Star(token, subject));
    }

    private void validateCanAddStar(Subject subject, String token) {
        Long starCount = starRepository.countAllByToken(token, Semester.getCurrentSemester());
        if (starCount >= MAX_STAR_NUMBER) {
            throw new AllcllException(AllcllErrorCode.STAR_LIMIT_EXCEEDED, MAX_STAR_NUMBER);
        }
        if (starRepository.existsBySubjectAndToken(subject, token, Semester.getCurrentSemester())) {
            throw new AllcllException(AllcllErrorCode.DUPLICATE_STAR, subject.getCuriNm());
        }
    }

    @Transactional
    public void deleteStarOnSubject(Long subjectId, String token) {
        starRepository.deleteStarBySubjectIdAndToken(subjectId, token, Semester.getCurrentSemester());
    }

    public StarredSubjectIdsResponse retrieveStars(String token) {
        List<Star> stars = starRepository.findAllByToken(token, Semester.getCurrentSemester());
        return new StarredSubjectIdsResponse(stars.stream()
            .map(star -> new StarredSubjectIdResponse(star.getSubject().getId()))
            .toList());
    }
}
