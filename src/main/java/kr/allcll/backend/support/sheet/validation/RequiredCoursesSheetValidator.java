package kr.allcll.backend.support.sheet.validation;

import java.util.List;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.support.sheet.GraduationSheetTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequiredCoursesSheetValidator implements GraduationSheetValidator {

    public static final String TAB_KEY = "required-courses";

    private static final List<String> REQUIRED_HEADERS = List.of(
        "admission_year",
        "admission_year_short",
        "dept_cd",
        "dept_nm",
        "category_type",
        "curi_no",
        "curi_nm",
        "required"
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
            graduationSheetValidationSupport.requireString(TAB_KEY, sheetTable, dataRow, rowIndex, "dept_cd");
            graduationSheetValidationSupport.requireString(TAB_KEY, sheetTable, dataRow, rowIndex, "dept_nm");
            graduationSheetValidationSupport.requireEnum(TAB_KEY, sheetTable, dataRow, rowIndex, "category_type",
                CategoryType.class);
            graduationSheetValidationSupport.requireString(TAB_KEY, sheetTable, dataRow, rowIndex, "curi_no");
            graduationSheetValidationSupport.requireString(TAB_KEY, sheetTable, dataRow, rowIndex, "curi_nm");
            graduationSheetValidationSupport.requireBoolean(TAB_KEY, sheetTable, dataRow, rowIndex, "required");
        }
    }
}
