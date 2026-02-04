package kr.allcll.backend.support.sheet.validation;

import java.util.List;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredArea;
import kr.allcll.backend.support.sheet.GraduationSheetTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BalanceRequiredCourseAreaMapSheetValidator implements GraduationSheetValidator {

    public static final String TAB_KEY = "balance-required-course-area-map";

    private static final List<String> REQUIRED_HEADERS = List.of(
        "admission_year",
        "admission_year_short",
        "curi_no",
        "curi_nm",
        "balance_required_area"
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
            graduationSheetValidationSupport.requireString(TAB_KEY, sheetTable, dataRow, rowIndex, "curi_no");
            graduationSheetValidationSupport.requireString(TAB_KEY, sheetTable, dataRow, rowIndex, "curi_nm");
            graduationSheetValidationSupport.requireEnum(TAB_KEY, sheetTable, dataRow, rowIndex,
                "balance_required_area", BalanceRequiredArea.class);
        }
    }
}
