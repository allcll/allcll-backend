package kr.allcll.backend.support.exception;

import lombok.Getter;

@Getter
public class AllcllSseException extends AllcllException {

    private final String errorCode;

    public AllcllSseException(AllcllErrorCode errorCode, Object... args) {
        super(errorCode, args);
        this.errorCode = errorCode.name();
    }
}
