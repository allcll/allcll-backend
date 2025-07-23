package kr.allcll.backend.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.allcll.backend.domain.seat.pin.Pin;
import kr.allcll.backend.domain.seat.pin.PinRepository;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.support.semester.Semester;
import kr.allcll.backend.support.sse.SseEmitterStorage;
import kr.allcll.crawler.seat.PinSubjectUpdateRequest;
import kr.allcll.crawler.seat.PinSubjectUpdateRequest.PinSubject;
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
        PinSubjectUpdateRequest request = getPinSubjects();
        externalClient.sendPinSubjects(request);
    }

    private PinSubjectUpdateRequest getPinSubjects() {
        List<String> tokens = sseEmitterStorage.getUserTokens();
        Map<Subject, Integer> pinSubjects = new HashMap<>();
        for (String token : tokens) {
            List<Pin> pins = pinRepository.findAllByToken(token, Semester.now());
            for (Pin pin : pins) {
                Subject subject = pin.getSubject();
                pinSubjects.merge(subject, 1, Integer::sum);
            }
        }
        List<PinSubject> wantPinSubjects = getPinSubjectsWithPriority(pinSubjects);
        return new PinSubjectUpdateRequest(wantPinSubjects);
    }

    private List<PinSubject> getPinSubjectsWithPriority(Map<Subject, Integer> pinSubjects) {
        List<Long> subjectIds = pinSubjects.keySet().stream()
            .sorted((o1, o2) -> pinSubjects.get(o2).compareTo(pinSubjects.get(o1)))
            .map(Subject::getId)
            .toList();

        int mapSize = pinSubjects.size();
        int firstIdx = mapSize / 3;
        int secondIdx = mapSize * 2 / 3;

        return grantPriorityToAllSubjects(subjectIds, firstIdx, secondIdx);
    }

    private List<PinSubject> grantPriorityToAllSubjects(List<Long> subjectIds, int firstIdx, int secondIdx) {
        List<PinSubject> result = new ArrayList<>();
        List<PinSubject> firstPrioritySubject = grantEachPriorityToSubjects(subjectIds.subList(0, firstIdx), 1);
        List<PinSubject> secondPrioritySubject = grantEachPriorityToSubjects(subjectIds.subList(firstIdx, secondIdx),
            2);
        List<PinSubject> thirdPrioritySubject = grantEachPriorityToSubjects(
            subjectIds.subList(secondIdx, subjectIds.size()), 3);
        result.addAll(firstPrioritySubject);
        result.addAll(secondPrioritySubject);
        result.addAll(thirdPrioritySubject);
        return result;
    }

    private List<PinSubject> grantEachPriorityToSubjects(List<Long> subjectIds, int priority) {
        return subjectIds.stream()
            .map(subjectId -> new PinSubject(subjectId, priority))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public void getTargetSubjectsFromCrawler() {
        externalClient.getAllTargetSubjects();
    }
}
