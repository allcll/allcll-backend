package kr.allcll.backend.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.allcll.backend.client.dto.PinSubject;
import kr.allcll.backend.client.dto.PinSubjectsRequest;
import kr.allcll.backend.domain.seat.pin.Pin;
import kr.allcll.backend.domain.seat.pin.PinRepository;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.support.sse.SseEmitterStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalService {

    private final PinRepository pinRepository;
    private final SseEmitterStorage sseEmitterStorage;
    private final ExternalClient externalClient;

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
