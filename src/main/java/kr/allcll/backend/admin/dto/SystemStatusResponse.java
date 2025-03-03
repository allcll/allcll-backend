package kr.allcll.backend.admin.dto;

public record SystemStatusResponse(
    boolean isSseConnect,
    boolean isNonMajorSending
) {

    public static SystemStatusResponse of(boolean isSseConnect, boolean isNonMajorSending) {
        return new SystemStatusResponse(isSseConnect, isNonMajorSending);
    }
}
