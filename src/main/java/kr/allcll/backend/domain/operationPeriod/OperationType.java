package kr.allcll.backend.domain.operationPeriod;

import lombok.Getter;

@Getter
public enum OperationType {
    TIMETABLE("timetable"),
    BASKETS("baskets"),
    SIMULATION("simulation"),
    LIVE("live"),
    PRESEAT("preseat"),
    GRADUATION("graduation");

    private final String value;

    OperationType(String value) {
        this.value = value;
    }
}
