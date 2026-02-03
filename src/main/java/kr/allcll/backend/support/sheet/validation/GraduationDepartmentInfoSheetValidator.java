package kr.allcll.backend.support.sheet.validation;

import java.util.List;
import kr.allcll.backend.domain.graduation.certification.CodingTargetType;
import kr.allcll.backend.domain.graduation.certification.EnglishTargetType;
import kr.allcll.backend.domain.graduation.department.DeptGroup;
import kr.allcll.backend.support.sheet.GraduationSheetTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GraduationDepartmentInfoSheetValidator implements GraduationSheetValidator {

    private static final String TAB_NAME = "graduation_department_info";

    private static final List<String> REQUIRED_HEADERS = List.of(
        "admission_year",
        "admission_year_short",
        "dept_nm",
        "college_nm",
        "dept_group",
        "english_target_type",
        "coding_target_type"
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
            graduationSheetValidationSupport.requireString(TAB_NAME, sheetTable, dataRow, rowIndex, "dept_nm");
            graduationSheetValidationSupport.requireString(TAB_NAME, sheetTable, dataRow, rowIndex, "dept_cd");
            graduationSheetValidationSupport.requireString(TAB_NAME, sheetTable, dataRow, rowIndex, "college_nm");
            graduationSheetValidationSupport.requireEnum(TAB_NAME, sheetTable, dataRow, rowIndex, "dept_group", DeptGroup.class);
            graduationSheetValidationSupport.requireEnum(TAB_NAME, sheetTable, dataRow, rowIndex, "english_target_type", EnglishTargetType.class);
            graduationSheetValidationSupport.requireEnum(TAB_NAME, sheetTable, dataRow, rowIndex, "coding_target_type", CodingTargetType.class);
        }
    }
}
