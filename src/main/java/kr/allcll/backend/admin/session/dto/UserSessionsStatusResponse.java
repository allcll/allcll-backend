package kr.allcll.backend.admin.session.dto;

import java.util.List;

public record UserSessionsStatusResponse(
    List<UserSessionStatusResponse> userSessionStatusResponses
) {

}
