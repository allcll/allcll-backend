package kr.allcll.backend.support.sheet.validation;


import java.util.List;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRuleType;
import kr.allcll.backend.support.sheet.GraduationSheetTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GraduationCertRulesSheetValidator implements GraduationSheetValidator {

    public static final String TAB_KEY = "graduation-cert-rules";

    private static final List<String> REQUIRED_HEADERS = List.of(
        "admission_year",
        "admission_year_short",
        "graduation_cert_rule_type",
        "required_pass_count",
        "enable_english",
        "enable_classic",
        "enable_coding"
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
            graduationSheetValidationSupport.requireEnum(TAB_KEY, sheetTable, dataRow, rowIndex, "graduation_cert_rule_type", GraduationCertRuleType.class);
            graduationSheetValidationSupport.requireInt(TAB_KEY, sheetTable, dataRow, rowIndex, "required_pass_count");
            graduationSheetValidationSupport.requireBoolean(TAB_KEY, sheetTable, dataRow, rowIndex, "enable_english");
            graduationSheetValidationSupport.requireBoolean(TAB_KEY, sheetTable, dataRow, rowIndex, "enable_classic");
            graduationSheetValidationSupport.requireBoolean(TAB_KEY, sheetTable, dataRow, rowIndex, "enable_coding");
        }
    }
}
