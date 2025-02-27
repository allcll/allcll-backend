package kr.allcll.backend.pin.dto;

import java.util.List;

public record SubjectIdsResponse(
    List<SubjectIdResponse> subjects
) {

}
