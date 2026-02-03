package kr.allcll.backend.support.sheet.validation;

import java.util.List;
import kr.allcll.backend.domain.graduation.certification.EnglishTargetType;
import kr.allcll.backend.support.sheet.GraduationSheetTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnglishCertCriteriaSheetValidator implements GraduationSheetValidator {

    private static final String TAB_NAME = "english_cert_criteria";

    private static final List<String> REQUIRED_HEADERS = List.of(
        "admission_year",
        "admission_year_short",
        "english_target_type",
        "toeic_min_score",
        "toefl_ibt_min_score",
        "teps_min_score",
        "new_teps_min_score",
        "opic_min_level",
        "toeic_speaking_min_level",
        "gtelp_level",
        "gtelp_min_score",
        "gtelp_speaking_level"
    );

    private final GraduationSheetValidationSupport graduationSheetValidationSupport;

    @Override
    public String tabName() {
        return TAB_NAME;
    }

    @Override
    public void validate(GraduationSheetTable sheetTable) {
        graduationSheetValidationSupport.validateNotEmpty(TAB_NAME, sheetTable);
        graduationSheetValidationSupport.validateRequiredHeaders(TAB_NAME, sheetTable, REQUIRED_HEADERS);

        List<List<Object>> dataRows = sheetTable.getDataRows();
        for (int rowIndex = 0; rowIndex < dataRows.size(); rowIndex++) {
            List<Object> dataRow = dataRows.get(rowIndex);

            graduationSheetValidationSupport.requireInt(TAB_NAME, sheetTable, dataRow, rowIndex, "admission_year");
            graduationSheetValidationSupport.requireInt(TAB_NAME, sheetTable, dataRow, rowIndex, "admission_year_short");
            graduationSheetValidationSupport.requireEnum(TAB_NAME, sheetTable, dataRow, rowIndex, "english_target_type", EnglishTargetType.class);
            graduationSheetValidationSupport.requireInt(TAB_NAME, sheetTable, dataRow, rowIndex, "toeic_min_score");
            graduationSheetValidationSupport.requireInt(TAB_NAME, sheetTable, dataRow, rowIndex, "toefl_ibt_min_score");
            graduationSheetValidationSupport.requireInt(TAB_NAME, sheetTable, dataRow, rowIndex, "teps_min_score");
            graduationSheetValidationSupport.requireInt(TAB_NAME, sheetTable, dataRow, rowIndex, "new_teps_min_score");
            graduationSheetValidationSupport.requireString(TAB_NAME, sheetTable, dataRow, rowIndex, "opic_min_level");
            graduationSheetValidationSupport.requireString(TAB_NAME, sheetTable, dataRow, rowIndex, "toeic_speaking_min_level");
            graduationSheetValidationSupport.requireInt(TAB_NAME, sheetTable, dataRow, rowIndex, "gtelp_level");
            graduationSheetValidationSupport.requireInt(TAB_NAME, sheetTable, dataRow, rowIndex, "gtelp_min_score");
            graduationSheetValidationSupport.requireInt(TAB_NAME, sheetTable, dataRow, rowIndex, "gtelp_speaking_level");
        }
    }
}

