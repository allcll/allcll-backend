package kr.allcll.backend.support.sheet.validation;

import java.util.List;
import kr.allcll.backend.support.sheet.GraduationSheetTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseReplacementsSheetValidator implements GraduationSheetValidator {

    public static final String TAB_KEY = "course-replacements";

    private static final List<String> REQUIRED_HEADERS = List.of(
        "admission_year",
        "admission_year_short",
        "legacy_curi_nm",
        "current_curi_no",
        "current_curi_nm",
        "enabled"
    );

    private final GraduationSheetValidationSupport graduationSheetValidationSupport;

    @Override
    public String tabKey() {
        return TAB_KEY;
    }

    @Override
    public void validate(GraduationSheetTable sheetTable) {
        graduationSheetValidationSupport.validateNotEmpty(TAB_KEY, sheetTable);
        graduationSheetValidationSupport.validateRequiredHeaders(TAB_KEY, sheetTable, REQUIRED_HEADERS);

        List<List<Object>> dataRows = sheetTable.getDataRows();
        for (int rowIndex = 0; rowIndex < dataRows.size(); rowIndex++) {
            List<Object> dataRow = dataRows.get(rowIndex);

            graduationSheetValidationSupport.requireInt(TAB_KEY, sheetTable, dataRow, rowIndex, "admission_year");
            graduationSheetValidationSupport.requireInt(TAB_KEY, sheetTable, dataRow, rowIndex, "admission_year_short");
            graduationSheetValidationSupport.requireString(TAB_KEY, sheetTable, dataRow, rowIndex, "legacy_curi_nm");
            graduationSheetValidationSupport.requireString(TAB_KEY, sheetTable, dataRow, rowIndex, "current_curi_no");
            graduationSheetValidationSupport.requireString(TAB_KEY, sheetTable, dataRow, rowIndex, "current_curi_nm");
            graduationSheetValidationSupport.requireBoolean(TAB_KEY, sheetTable, dataRow, rowIndex, "enabled");
        }
    }
}
