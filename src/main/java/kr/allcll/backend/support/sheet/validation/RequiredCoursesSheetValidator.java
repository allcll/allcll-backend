package kr.allcll.backend.support.sheet.validation;

import java.util.List;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.support.sheet.GraduationSheetTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequiredCoursesSheetValidator implements GraduationSheetValidator {

    private static final String TAB_NAME = "required-courses";

    private static final List<String> REQUIRED_HEADERS = List.of(
        "admission_year",
        "admission_year_short",
        "dept_cd",
        "category_type",
        "curi_no",
        "curi_nm",
        "required"
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
            graduationSheetValidationSupport.requireString(TAB_NAME, sheetTable, dataRow, rowIndex, "dept_cd");
            graduationSheetValidationSupport.requireEnum(TAB_NAME, sheetTable, dataRow, rowIndex, "category_type", CategoryType.class);
            graduationSheetValidationSupport.requireString(TAB_NAME, sheetTable, dataRow, rowIndex, "curi_no");
            graduationSheetValidationSupport.requireString(TAB_NAME, sheetTable, dataRow, rowIndex, "curi_nm");
            graduationSheetValidationSupport.requireBoolean(TAB_NAME, sheetTable, dataRow, rowIndex, "required");
        }
    }
}

