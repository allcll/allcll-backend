package kr.allcll.backend.subject.dto;

import java.util.List;
import kr.allcll.backend.subject.Subject;

public record SubjectsResponse(
    List<SubjectResponse> subjectResponses
) {

    public static SubjectsResponse from(List<Subject> subjects) {
        return new SubjectsResponse(subjects.stream()
            .map(SubjectResponse::from)
            .toList());
    }
}
