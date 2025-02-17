package kr.allcll.seatfinder.external;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.allcll.seatfinder.basket.Basket;
import kr.allcll.seatfinder.basket.BasketRepository;
import kr.allcll.seatfinder.exception.AllcllErrorCode;
import kr.allcll.seatfinder.exception.AllcllException;
import kr.allcll.seatfinder.external.dto.NonMajorRequest;
import kr.allcll.seatfinder.external.dto.PinSubject;
import kr.allcll.seatfinder.external.dto.PinSubjectsRequest;
import kr.allcll.seatfinder.pin.Pin;
import kr.allcll.seatfinder.pin.PinRepository;
import kr.allcll.seatfinder.sse.SseEmitterStorage;
import kr.allcll.seatfinder.subject.Subject;
import kr.allcll.seatfinder.subject.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalService {

    private static final String NON_MAJOR_DEPT_CD = "9005";
    private static final int SUBJECT_OFFER_COUNT = 20;

    private final SubjectRepository subjectRepository;
    private final BasketRepository basketRepository;
    private final PinRepository pinRepository;
    private final TopNonMajorStorage topNonMajorStorage;
    private final SseEmitterStorage sseEmitterStorage;
    private final ExternalClient externalClient;

    public void saveNonMajorSubjects() {
        List<Subject> nonMajorSubjects = subjectRepository.findAllByDeptCd(NON_MAJOR_DEPT_CD);
        Map<Subject, Integer> nonMajorSeats = getNonMajorSeats(nonMajorSubjects);
        List<Subject> topSubjects = getTopSubjects(nonMajorSeats);
        topNonMajorStorage.addAll(topSubjects);
        log.info("저장한 교양 과목 수: {}", topSubjects.size());
    }

    private Map<Subject, Integer> getNonMajorSeats(List<Subject> nonMajorSubjects) {
        Map<Subject, Integer> nonMajorSeats = new HashMap<>();
        for (Subject subject : nonMajorSubjects) {
            Long subjectId = subject.getId();
            Basket basket = basketRepository.findBySubjectId(subjectId).stream()
                .findAny()
                .orElseThrow(() -> new AllcllException(AllcllErrorCode.BASKET_NOT_FOUND));
            Integer seatCount = basket.getTotRcnt();
            nonMajorSeats.put(subject, seatCount);
        }
        return nonMajorSeats;
    }

    private List<Subject> getTopSubjects(Map<Subject, Integer> nonMajorSeats) {
        return nonMajorSeats.keySet().stream()
            .sorted((o1, o2) -> nonMajorSeats.get(o2).compareTo(nonMajorSeats.get(o1)))
            .limit(SUBJECT_OFFER_COUNT)
            .toList();
    }

    public void sendNonMajorToExternal() {
        List<Subject> topNonMajors = topNonMajorStorage.getSubjects();
        externalClient.sendNonMajor(NonMajorRequest.from(topNonMajors));
    }

    public void sendWantPinSubjectIdsToCrawler() {
        PinSubjectsRequest request = getPinSubjects();
        externalClient.sendPinSubjects(request);
    }

    private PinSubjectsRequest getPinSubjects() {
        List<String> tokens = sseEmitterStorage.getUserTokens();
        Map<Subject, Integer> pinSubjects = new HashMap<>();
        for (String token : tokens) {
            List<Pin> pins = pinRepository.findAllByToken(token);
            for (Pin pin : pins) {
                Subject subject = pin.getSubject();
                pinSubjects.merge(subject, 1, Integer::sum);
            }
        }
        List<PinSubject> wantPinSubjects = getWantPinSubjects(pinSubjects);
        return PinSubjectsRequest.from(wantPinSubjects);
    }

    private List<PinSubject> getWantPinSubjects(Map<Subject, Integer> pinSubjects) {
        List<Long> subjectIds = pinSubjects.keySet().stream()
            .sorted((o1, o2) -> pinSubjects.get(o2).compareTo(pinSubjects.get(o1)))
            .map(Subject::getId)
            .toList();

        int mapSize = pinSubjects.size();
        int firstIdx = mapSize / 3;
        int secondIdx = mapSize * 2 / 3;

        List<PinSubject> firstPrioritySubject = getPrioritySubject(subjectIds.subList(0, firstIdx), 1);
        List<PinSubject> secondPrioritySubject = getPrioritySubject(subjectIds.subList(firstIdx, secondIdx), 2);
        List<PinSubject> thirdPrioritySubject = getPrioritySubject(subjectIds.subList(secondIdx, subjectIds.size()), 3);
        firstPrioritySubject.addAll(secondPrioritySubject);
        firstPrioritySubject.addAll(thirdPrioritySubject);
        return firstPrioritySubject;
    }

    private List<PinSubject> getPrioritySubject(List<Long> subjectIds, int priority) {
        return subjectIds.stream()
            .map(subjectId -> new PinSubject(subjectId, priority))
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
