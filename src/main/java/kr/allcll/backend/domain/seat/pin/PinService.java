package kr.allcll.backend.domain.seat.pin;

import java.util.List;
import kr.allcll.backend.domain.seat.pin.dto.SubjectIdResponse;
import kr.allcll.backend.domain.seat.pin.dto.SubjectIdsResponse;
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
public class PinService {

    private static final int MAX_PIN_NUMBER = 5;

    private final PinRepository pinRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public void addPinOnSubject(Long subjectId, String token) {
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.SUBJECT_NOT_FOUND, subjectId));
        validateCanAddPin(subject, token);
        pinRepository.save(new Pin(token, subject));
    }

    private void validateCanAddPin(Subject subject, String token) {
        Long pinCount = pinRepository.countAllByToken(token, Semester.now());
        if (pinCount >= MAX_PIN_NUMBER) {
            throw new AllcllException(AllcllErrorCode.PIN_LIMIT_EXCEEDED, MAX_PIN_NUMBER);
        }
        if (pinRepository.existsBySubjectAndToken(subject, token, Semester.now())) {
            throw new AllcllException(AllcllErrorCode.DUPLICATE_PIN, subject.getCuriNm());
        }
    }

    @Transactional
    public void deletePinOnSubject(Long subjectId, String token) {
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.SUBJECT_NOT_FOUND, subjectId));
        Pin pin = pinRepository.findBySubjectAndToken(subject, token, Semester.now())
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.PIN_SUBJECT_MISMATCH));
        pinRepository.deleteById(pin.getId());
    }

    public SubjectIdsResponse retrievePins(String token) {
        List<Pin> pins = pinRepository.findAllByToken(token, Semester.now());
        return new SubjectIdsResponse(pins.stream()
            .map(pin -> new SubjectIdResponse(pin.getSubject().getId()))
            .toList());
    }
}
