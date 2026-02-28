package kr.allcll.backend.domain.operationPeriod;

import java.util.Arrays;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.Getter;

@Getter
public enum OperationType {
    TIMETABLE("timetable", "시간표"),
    BASKETS("baskets", "관심과목"),
    SIMULATION("simulation", "올클연습"),
    LIVE("live", "실시간여석"),
    PRESEAT("preseat", "여석선공개"),
    GRADUATION("graduation", "졸업요건검사");

    private final String value;
    private final String name;

    OperationType(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static OperationType from(String operationName) {
        return Arrays.stream(values())
            .filter(operationType -> operationType.value.equalsIgnoreCase(operationName.trim()))
            .findFirst()
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.OPERATION_TYPE_NOT_FOUND));
    }
}
