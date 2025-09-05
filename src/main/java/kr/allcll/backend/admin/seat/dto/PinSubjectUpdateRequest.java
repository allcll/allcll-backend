package kr.allcll.backend.admin.seat.dto;

import java.util.List;

public record PinSubjectUpdateRequest(
    List<PinSubject> subjects
) {

    public record PinSubject(
        Long subjectId,
        int priority
    ) {

    }
}
