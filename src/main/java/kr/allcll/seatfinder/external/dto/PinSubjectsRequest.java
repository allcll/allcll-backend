package kr.allcll.seatfinder.external.dto;

import java.util.List;

public record PinSubjectsRequest(
    List<PinSubject> subjects
) {

    public static PinSubjectsRequest from(List<PinSubject> pinSubjects) {
        return new PinSubjectsRequest(pinSubjects);
    }
}
