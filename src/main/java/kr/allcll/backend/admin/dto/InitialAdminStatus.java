package kr.allcll.backend.admin.dto;

public record InitialAdminStatus(
    boolean sseStatus,
    boolean nonMajorStatus
) {

    public static InitialAdminStatus from(boolean sseStatus, boolean nonMajorStatus) {
        return new InitialAdminStatus(sseStatus, nonMajorStatus);
    }
}
