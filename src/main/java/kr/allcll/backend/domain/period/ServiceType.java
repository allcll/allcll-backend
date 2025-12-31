package kr.allcll.backend.domain.period;

import lombok.Getter;

@Getter
public enum ServiceType {
    TIMETABLE("timetable"),
    BASKETS("baskets"),
    SIMULATION("simulation"),
    LIVE("live"),
    PRESEAT("preseat");

    private final String value;

    ServiceType(String value) {
        this.value = value;
    }
}
