package kr.allcll.backend.admin.session.dto;

public record UserSessionStatusResponse(
    String userId,
    boolean isActive
) {

}
