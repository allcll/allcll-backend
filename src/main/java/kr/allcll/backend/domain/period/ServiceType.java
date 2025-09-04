package kr.allcll.backend.domain.period;

import java.util.Arrays;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
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

    public static ServiceType fromValue(String value) {
        return Arrays.stream(ServiceType.values())
            .filter(s -> s.getValue().equals(value))
            .findFirst()
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.INVALID_SERVICE_TYPE));
    }
    
}
