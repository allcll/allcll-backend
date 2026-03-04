package kr.allcll.backend.domain.graduation.check.cert;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum ClassicsArea {
    WESTERN("서양의 역사와 사상", 4),
    EASTERN("동양의 역사와 사상", 2),
    EASTERN_AND_WESTERN("동·서양의 문학", 3),
    SCIENCE("과학 사상", 1);

    private final String koreanName;
    private final int maxRecognizedCount;

    public static Optional<ClassicsArea> findByLabel(String label) {
        return Arrays.stream(values())
            .filter(area -> label.contains(area.koreanName))
            .findFirst();
    }

    public int getRecognizedCount(int actualCount) {
        return Math.min(actualCount, maxRecognizedCount);
    }
}
