package kr.allcll.backend.admin.session.dto;

public record SessionStatusResponse(
    boolean isActive
) {

    public static SessionStatusResponse of(boolean isActive) {
        return new SessionStatusResponse(isActive);
    }
}
