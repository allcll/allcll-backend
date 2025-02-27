package kr.allcll.backend.domain.subject.dto;

import java.util.List;
import kr.allcll.backend.domain.subject.Subject;

public record SubjectsResponse(
    List<SubjectResponse> subjectResponses
) {

    public static SubjectsResponse from(List<Subject> subjects) {
        return new SubjectsResponse(subjects.stream()
            .map(SubjectResponse::from)
            .toList());
    }
}
