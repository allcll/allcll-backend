package kr.allcll.backend.domain.period;

import lombok.Getter;

@Getter
public enum OperationType {
    TIMETABLE("timetable"),
    BASKETS("baskets"),
    SIMULATION("simulation"),
    LIVE("live"),
    PRESEAT("preseat");

    private final String value;

    OperationType(String value) {
        this.value = value;
    }
}
