package kr.allcll.backend.support.exception;

import lombok.Getter;

@Getter
public class AllcllException extends RuntimeException {

    private final AllcllErrorCode errorCode;

    public AllcllException(AllcllErrorCode errorCode, Object... args) {
        super(String.format(errorCode.getMessage(), args));
        this.errorCode = errorCode;
    }
}
