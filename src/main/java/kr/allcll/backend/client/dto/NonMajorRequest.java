package kr.allcll.backend.client.dto;

import java.util.List;
import kr.allcll.backend.domain.subject.Subject;

public record NonMajorRequest(
    List<Long> subjectIds
) {

    public static NonMajorRequest from(List<Subject> subjects) {
        List<Long> subjectIds = subjects.stream()
            .map(Subject::getId)
            .toList();
        return new NonMajorRequest(subjectIds);
    }
}
