package kr.allcll.backend.support.sheet;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.util.List;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GraduationSheetsFetcher {

    public static final String SHEETRANGE = "!A:Z";

    private final Sheets sheets;
    private final GraduationSheetsProperties graduationSheetsProperties;

    public GraduationSheetTable fetchAsTable(String tabName) {
        try {
            String range = tabName + SHEETRANGE;
            ValueRange response = sheets.spreadsheets().values()
                .get(graduationSheetsProperties.spreadsheetId(), range)
                .execute();

            List<List<Object>> values = response.getValues();
            return GraduationSheetTable.from(values);
        } catch (Exception e) {
            throw new AllcllException(AllcllErrorCode.GOOGLE_SHEET_TAP_NOT_FOUND);
        }
    }
}

