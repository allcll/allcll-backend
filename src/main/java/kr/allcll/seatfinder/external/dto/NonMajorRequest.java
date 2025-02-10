package kr.allcll.seatfinder.external.dto;

import java.util.List;
import kr.allcll.seatfinder.subject.Subject;

public record NonMajorRequest(
    List<Long> wantNonMajorSubjectIds
) {

    public static NonMajorRequest from(List<Subject> subjects) {
        List<Long> subjectIds = subjects.stream()
            .map(Subject::getId)
            .toList();
        return new NonMajorRequest(subjectIds);
    }
}
