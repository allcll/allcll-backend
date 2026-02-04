package kr.allcll.backend.support.sheet;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.util.List;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GraduationSheetFetcher {

    public static final String TAB_RANGE_SUFFIX = "!A:Z";

    private final Sheets sheets;
    private final GraduationSheetProperties graduationSheetProperties;

    public GraduationSheetTable fetchAsTable(String tabName) {
        try {
            String sheetRange = tabName + TAB_RANGE_SUFFIX;
            ValueRange valueRange = sheets.spreadsheets().values()
                .get(graduationSheetProperties.spreadsheetId(), sheetRange)
                .execute();

            List<List<Object>> tableValues = valueRange.getValues();
            return GraduationSheetTable.from(tableValues);
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new AllcllException(AllcllErrorCode.GOOGLE_SHEET_TAP_NOT_FOUND, e);
            }
            throw new AllcllException(AllcllErrorCode.GOOGLE_SHEET_ERROR, e);
        }  catch (Exception e) {
            throw new AllcllException(AllcllErrorCode.GOOGLE_SHEET_ERROR);
        }
    }
}
