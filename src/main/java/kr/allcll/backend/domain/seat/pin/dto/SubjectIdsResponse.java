package kr.allcll.backend.domain.seat.pin.dto;

import java.util.List;

public record SubjectIdsResponse(
    List<SubjectIdResponse> subjects
) {

}
