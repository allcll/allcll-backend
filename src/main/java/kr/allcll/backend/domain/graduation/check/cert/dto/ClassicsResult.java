package kr.allcll.backend.domain.graduation.check.cert.dto;

public record ClassicsResult(
    boolean passed,
    ClassicsCounts counts
) {

    public static ClassicsResult empty() {
        return new ClassicsResult(false, ClassicsCounts.empty());
    }

    public static ClassicsResult passedWith(ClassicsCounts fallbackCounts) {
        return new ClassicsResult(true, fallbackCounts);
    }

    public static ClassicsResult failedWith(ClassicsCounts fallbackCounts) {
        return new ClassicsResult(false, fallbackCounts);
    }

    public ClassicsResult withFallbackCounts(ClassicsCounts fallbackCounts) {
        if (counts == null) {
            return new ClassicsResult(passed, fallbackCounts);
        }
        return this;
    }
}
