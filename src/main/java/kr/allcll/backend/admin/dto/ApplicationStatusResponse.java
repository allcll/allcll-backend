package kr.allcll.backend.admin.dto;

public record ApplicationStatusResponse(
    boolean isSseConnect,
    boolean isNonMajorSending
) {

    public static ApplicationStatusResponse from(boolean isSseConnect, boolean isNonMajorSending) {
        return new ApplicationStatusResponse(isSseConnect, isNonMajorSending);
    }
}
