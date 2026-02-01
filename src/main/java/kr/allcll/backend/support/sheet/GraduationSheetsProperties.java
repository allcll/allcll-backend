package kr.allcll.backend.support.sheet;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.sheets")
public record GraduationSheetsProperties(
    String applicationName,
    String credentialsLocation,
    String spreadsheetId,
    Map<String, String> tabs
) {

}

