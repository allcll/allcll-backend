package kr.allcll.backend.star.dto;

import java.util.List;

public record StarredSubjectIdsResponse(
    List<StarredSubjectIdResponse> subjects
) {

}
