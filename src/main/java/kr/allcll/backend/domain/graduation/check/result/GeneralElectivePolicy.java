package kr.allcll.backend.domain.graduation.check.result;

import java.util.Set;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import org.springframework.stereotype.Component;

@Component
public class GeneralElectivePolicy {

    private static final String SEJONG_CYBER_CURI_NO_PREFIX = "5";
    private static final Set<String> SEJONG_CYBER_EXCEPTION_CURI_NOS = Set.of("501335");
    private static final int SEJONG_CYBER_GENERAL_ELECTIVE_EFFECTIVE_ADMISSION_YEAR = 2022;

    boolean shouldExcludeFromGeneralElective(int admissionYear, CategoryType categoryType, String curiNo) {
        if (!CategoryType.GENERAL_ELECTIVE.equals(categoryType)) {
            return false;
        }
        if (admissionYear >= SEJONG_CYBER_GENERAL_ELECTIVE_EFFECTIVE_ADMISSION_YEAR) {
            return false;
        }
        return isSejongCyberCourse(curiNo);
    }

    private boolean isSejongCyberCourse(String curiNo) {
        if (SEJONG_CYBER_EXCEPTION_CURI_NOS.contains(curiNo)) {
            return false;
        }
        return curiNo.startsWith(SEJONG_CYBER_CURI_NO_PREFIX);
    }
}
