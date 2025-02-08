package kr.allcll.seatfinder.star;

import java.util.List;
import kr.allcll.seatfinder.exception.AllcllErrorCode;
import kr.allcll.seatfinder.exception.AllcllException;
import kr.allcll.seatfinder.star.dto.StarredSubjectIdResponse;
import kr.allcll.seatfinder.star.dto.StarredSubjectIdsResponse;
import kr.allcll.seatfinder.subject.Subject;
import kr.allcll.seatfinder.subject.SubjectRepository;
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
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.SUBJECT_NOT_FOUND));
        validateCanAddStar(subject, token);
        starRepository.save(new Star(token, subject));
    }

    private void validateCanAddStar(Subject subject, String token) {
        Long starCount = starRepository.countAllByToken(token);
        if (starCount >= MAX_STAR_NUMBER) {
            throw new AllcllException(AllcllErrorCode.STAR_LIMIT_EXCEEDED, MAX_STAR_NUMBER);
        }
        if (starRepository.existsBySubjectAndToken(subject, token)) {
            throw new AllcllException(AllcllErrorCode.DUPLICATE_STAR, subject.getCuriNm());
        }
    }

    @Transactional
    public void deleteStarOnSubject(Long subjectId, String token) {
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.SUBJECT_NOT_FOUND));
        starRepository.deleteStarBySubjectAndToken(subject, token);
    }

    public StarredSubjectIdsResponse retrieveStars(String token) {
        List<Star> stars = starRepository.findAllByToken(token);
        return new StarredSubjectIdsResponse(stars.stream()
            .map(star -> new StarredSubjectIdResponse(star.getSubject().getId()))
            .toList());
    }
}
