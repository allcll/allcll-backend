package kr.allcll.backend.domain.graduation.certification;

public enum GradeThreshold {

    PASS("P", null),
    NON_PASS("NP", null),
    A_PLUS("A+", 9),
    A0("A0", 8),
    B_PLUS("B+", 7),
    B0("B0", 6),
    C_PLUS("C+", 5),
    C0("C0", 4),
    D_PLUS("D+", 3),
    D0("D0", 2),
    F("F", 0);

    private final String grade;
    private final Integer score;

    GradeThreshold(String grade, Integer score) {
        this.grade = grade;
        this.score = score;
    }

    public static GradeThreshold from(String grade) {
        String code = grade.trim().toUpperCase();
        for (GradeThreshold requirement : values()) {
            if (requirement.grade.equals(code)) {
                return requirement;
            }
        }
        return null;
    }

    public boolean satisfiedMinGrade(String actualGrade) {
        GradeThreshold actual = from(actualGrade);
        if (actual == null) {
            return false;
        }
        if (NON_PASS.equals(actual)) {
            return false;
        }
        if (PASS.equals(this)) {
            return PASS.equals(actual);
        }
        if (this.score == null || actual.score == null) {
            return false;
        }
        return actual.score >= this.score;
    }
}
