package kr.allcll.backend.support.sheet;

import java.util.Map;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.sheets")
public record GraduationSheetProperties(
    String applicationName,
    String credentialsLocation,
    String spreadsheetId,
    Map<String, String> tabs
) {

    public String tabName(String tabKey) {
        String tabName = tabs.get(tabKey);
        if (tabName == null || tabName.isBlank()) {
            throw new AllcllException(AllcllErrorCode.GOOGLE_SHEET_TAB_NOT_FOUND);
        }
        return tabName;
    }
}
