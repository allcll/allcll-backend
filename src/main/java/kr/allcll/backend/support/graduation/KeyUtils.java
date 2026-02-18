package kr.allcll.backend.support.graduation;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class KeyUtils {

    private static final String KEY_DELIMITER = "|";

    public static String generate(Object... parts) {
        return Arrays.stream(parts)
            .filter(Objects::nonNull)
            .map(Object::toString)
            .collect(Collectors.joining(KEY_DELIMITER));
    }
}
