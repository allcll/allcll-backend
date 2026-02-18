package kr.allcll.backend.domain.graduation.credit;

import org.springframework.stereotype.Component;

@Component
public class GeneralElectivePolicy {

    private static final String SEJONG_CYBER_CURI_NO_PREFIX = "5";
    private static final int SEJONG_CYBER_GENERAL_ELECTIVE_EFFECTIVE_ADMISSION_YEAR = 2022;

    public boolean shouldExcludeFromGeneralElective(int admissionYear, CategoryType categoryType, String curiNo) {
        if (!CategoryType.GENERAL_ELECTIVE.equals(categoryType)) {
            return false;
        }
        if (admissionYear >= SEJONG_CYBER_GENERAL_ELECTIVE_EFFECTIVE_ADMISSION_YEAR) {
            return false;
        }
        return isSejongCyberCourse(curiNo);
    }

    private boolean isSejongCyberCourse(String curiNo) {
        if (curiNo == null || curiNo.isBlank()) {
            return false;
        }
        return curiNo.startsWith(SEJONG_CYBER_CURI_NO_PREFIX);
    }
}
