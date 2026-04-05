package kr.allcll.backend.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ToscResponse(
    boolean success,
    Map<String, String> errors
) {

    public String getErrorMessage() {
        if (errors == null || errors.isEmpty()) {
            return "알 수 없는 오류";
        }
        return String.join(", ", errors.values());
    }
}
