package kr.allcll.backend.support.sheet.validation;

import java.util.List;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.support.sheet.GraduationSheetTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreditCriteriaSheetValidator implements GraduationSheetValidator {

    private static final String TAB_NAME = "credit_criteria";

    private static final List<String> REQUIRED_HEADERS = List.of(
        "admission_year",
        "admission_year_short",
        "major_type",
        "dept_cd",
        "major_scope",
        "category_type",
        "required_credits",
        "enabled"
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
            graduationSheetValidationSupport.requireEnum(TAB_NAME, sheetTable, dataRow, rowIndex, "major_type", MajorType.class);
            graduationSheetValidationSupport.requireString(TAB_NAME, sheetTable, dataRow, rowIndex, "dept_cd");
            graduationSheetValidationSupport.requireEnum(TAB_NAME, sheetTable, dataRow, rowIndex, "major_scope", MajorScope.class);
            graduationSheetValidationSupport.requireEnum(TAB_NAME, sheetTable, dataRow, rowIndex, "category_type", CategoryType.class);
            graduationSheetValidationSupport.requireInt(TAB_NAME, sheetTable, dataRow, rowIndex, "required_credits");
            graduationSheetValidationSupport.requireBoolean(TAB_NAME, sheetTable, dataRow, rowIndex, "enabled");
        }
    }
}
