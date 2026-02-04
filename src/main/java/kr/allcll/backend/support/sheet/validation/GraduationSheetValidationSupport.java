package kr.allcll.backend.support.sheet.validation;

import java.util.List;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.sheet.GraduationSheetTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GraduationSheetValidationSupport {

    private static final int SHEET_DATA_ROW_OFFSET = 2;

    public void validateNotEmpty(String tabKey, GraduationSheetTable sheetTable) {
        if (sheetTable == null || sheetTable.getDataRows() == null || sheetTable.getDataRows().isEmpty()) {
            log.error("[구글시트 검증 실패] 시트 데이터가 비어있습니다. tabKey={}", tabKey);
            throw new AllcllException(AllcllErrorCode.GOOGLE_SHEET_EMPTY);
        }
    }

    public void validateRequiredHeaders(String tabKey, GraduationSheetTable sheetTable, List<String> requiredHeaders) {
        if (sheetTable == null || sheetTable.getHeaderToIndex() == null) {
            log.error("[구글시트 검증 실패] 헤더가 존재하지 않습니다. tabKey={}", tabKey);
            throw new AllcllException(AllcllErrorCode.GOOGLE_SHEET_INVALID_HEADER);
        }
        for (String seatHeader : requiredHeaders) {
            if (!sheetTable.getHeaderToIndex().containsKey(seatHeader)) {
                log.error("[구글시트 검증 실패] 필수 헤더가 누락되었습니다. tabKey={} headerName={}", tabKey, seatHeader);
                throw new AllcllException(AllcllErrorCode.GOOGLE_SHEET_INVALID_HEADER);
            }
        }
    }

    public String requireString(String tabKey, GraduationSheetTable sheetTable, List<Object> dataRow,
        int rowIndex, String sheetHeader) {
        String sheetValue = sheetTable.getString(dataRow, sheetHeader);
        if (sheetValue == null) {
            logRowError(tabKey, rowIndex, sheetHeader, dataRow, "문자열 값이 비어있거나 null 입니다.");
            throw new AllcllException(AllcllErrorCode.GOOGLE_SHEET_INVALID_ROW);
        }
        return sheetValue;
    }

    public Integer requireInt(String tabKey, GraduationSheetTable sheetTable, List<Object> dataRow,
        int rowIndex, String sheetHeader) {
        try {
            Integer sheetValue = sheetTable.getInt(dataRow, sheetHeader);
            if (sheetValue == null) {
                logRowError(tabKey, rowIndex, sheetHeader, dataRow, "정수 값이 null 입니다.");
                throw new AllcllException(AllcllErrorCode.GOOGLE_SHEET_INVALID_ROW);
            }
            return sheetValue;
        } catch (Exception e) {
            logRowError(tabKey, rowIndex, sheetHeader, dataRow, "정수 파싱에 실패했습니다.");
            throw new AllcllException(AllcllErrorCode.GOOGLE_SHEET_INVALID_ROW);
        }
    }

    public Boolean requireBoolean(String tabKey, GraduationSheetTable sheetTable, List<Object> dataRow,
        int rowIndex, String sheetHeader) {
        try {
            Boolean sheetValue = sheetTable.getBoolean(dataRow, sheetHeader);
            if (sheetValue == null) {
                logRowError(tabKey, rowIndex, sheetHeader, dataRow, "Boolean 값이 null 입니다.");
                throw new AllcllException(AllcllErrorCode.GOOGLE_SHEET_INVALID_ROW);
            }
            return sheetValue;
        } catch (Exception e) {
            logRowError(tabKey, rowIndex, sheetHeader, dataRow, "Boolean 파싱에 실패했습니다.");
            throw new AllcllException(AllcllErrorCode.GOOGLE_SHEET_INVALID_ROW);
        }
    }

    public <T extends Enum<T>> T requireEnum(String tabKey, GraduationSheetTable sheetTable, List<Object> dataRow,
        int rowIndex, String sheetHeader, final Class<T> enumType) {
        try {
            T sheetValue = sheetTable.getEnum(dataRow, sheetHeader, enumType);
            if (sheetValue == null) {
                logRowError(tabKey, rowIndex, sheetHeader, dataRow, "Enum 값이 null 입니다.");
                throw new AllcllException(AllcllErrorCode.GOOGLE_SHEET_INVALID_ROW);
            }
            return sheetValue;
        } catch (Exception e) {
            logRowError(tabKey, rowIndex, sheetHeader, dataRow, "Enum 파싱에 실패했습니다.");
            throw new AllcllException(AllcllErrorCode.GOOGLE_SHEET_INVALID_ROW);
        }
    }

    private void logRowError(final String tabKey, final int rowIndex, final String header,
        final List<Object> dataRow, final String message) {
        int sheetRow = rowIndex + SHEET_DATA_ROW_OFFSET;
        log.error(
            "[구글시트 검증 실패] tab={}, column={}, row={}, 값={}, 메세지={}",
            tabKey, header, sheetRow, dataRow, message
        );
    }
}
