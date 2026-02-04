package kr.allcll.backend.support.sheet.validation;

import java.util.List;
import kr.allcll.backend.support.sheet.GraduationSheetTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClassicCertCriteriaSheetValidator implements GraduationSheetValidator {

    public static final String TAB_KEY = "classic-cert-criteria";

    private static final List<String> REQUIRED_HEADERS = List.of(
        "admission_year",
        "admission_year_short",
        "total_required_count",
        "required_count_western",
        "required_count_eastern",
        "required_count_eastern_and_western",
        "required_count_science"
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
            graduationSheetValidationSupport.requireInt(TAB_KEY, sheetTable, dataRow, rowIndex, "total_required_count");
            graduationSheetValidationSupport.requireInt(TAB_KEY, sheetTable, dataRow, rowIndex, "required_count_western");
            graduationSheetValidationSupport.requireInt(TAB_KEY, sheetTable, dataRow, rowIndex, "required_count_eastern");
            graduationSheetValidationSupport.requireInt(TAB_KEY, sheetTable, dataRow, rowIndex, "required_count_eastern_and_western");
            graduationSheetValidationSupport.requireInt(TAB_KEY, sheetTable, dataRow, rowIndex, "required_count_science");
        }
    }
}
