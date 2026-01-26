package kr.allcll.backend.client;

import java.util.List;
import kr.allcll.backend.admin.seat.AllSeatBuffer;
import kr.allcll.backend.admin.seat.TargetSubjectService;
import kr.allcll.backend.admin.seat.dto.ChangeSubjectsResponse;
import kr.allcll.backend.admin.seat.dto.PinSubjectUpdateRequest;
import kr.allcll.backend.domain.seat.SeatStorage;
import kr.allcll.backend.domain.seat.dto.SeatDto;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.subject.SubjectRepository;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.semester.Semester;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(ExternalProperties.class)
public class ExternalClient {

    // 외부 의존성
    private final TargetSubjectService targetSubjectService;
    //    private final ChangedSubjectBuffer changedSubjectBuffer;
    private final AllSeatBuffer allSeatBuffer;

    private final SeatStorage seatStorage;
    private final SubjectRepository subjectRepository;

    public void sendPinSubjects(PinSubjectUpdateRequest request) {
        targetSubjectService.loadPinSubjects(request);
    }

    public void getAllTargetSubjects() {
        List<ChangeSubjectsResponse> allChangedSubject = allSeatBuffer.getAllAndFlush();
        for (ChangeSubjectsResponse eachChange : allChangedSubject) {
            Long subjectId = eachChange.subjectId();
            Subject subject = subjectRepository.findById(subjectId, Semester.getCurrentSemester())
                .orElseThrow(() -> new AllcllException(AllcllErrorCode.SUBJECT_NOT_FOUND, subjectId));
            seatStorage.add(
                new SeatDto(subject,
                    eachChange.remainSeat(),
                    eachChange.createdAt()
//                    eachChange.changeStatus()
                )
            );
        }
    }
}
