package kr.allcll.backend.domain.basket.star.dto;

import java.util.List;

public record StarredSubjectIdsResponse(
    List<StarredSubjectIdResponse> subjects
) {

}
