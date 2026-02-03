package kr.allcll.backend.support.sheet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class GraduationSheetTable {

    private static final String TRUE_FLAG = "1";
    private static final int HEADER_ROW_INDEX = 0;
    private static final int FIRST_DATA_ROW_INDEX = HEADER_ROW_INDEX + 1;


    private final Map<String, Integer> headerToIndex;
    private final List<List<Object>> dataRows;

    private GraduationSheetTable(Map<String, Integer> headerToIndex, List<List<Object>> dataRows) {
        this.headerToIndex = headerToIndex;
        this.dataRows = dataRows;
    }

    public static GraduationSheetTable from(List<List<Object>> tableValues) {
        if (tableValues == null || tableValues.isEmpty()) {
            return new GraduationSheetTable(Map.of(), List.of());
        }

        Map<String, Integer> headerToIndex = buildHeaderIndex(tableValues.getFirst());
        List<List<Object>> dataRows = extractDataRows(tableValues);

        return new GraduationSheetTable(headerToIndex, dataRows);
    }

    public String getString(List<Object> row, String header) {
        Object cell = getCell(row, header);
        if (cell == null) {
            return null;
        }

        String value = String.valueOf(cell).trim();
        if (value.isBlank()) {
            return null;
        }

        return value;
    }

    public Integer getInt(List<Object> row, String header) {
        String value = getString(row, header);
        if (value == null) {
            return null;
        }
        return Integer.valueOf(value);
    }

    public Boolean getBoolean(List<Object> row, String header) {
        String value = getString(row, header);
        if (value == null) {
            return null;
        }

        if (TRUE_FLAG.equals(value)) {
            return true;
        }
        return false;
    }

    public <T extends Enum<T>> T getEnum(List<Object> row, String header, Class<T> type) {
        String value = getString(row, header);
        if (value == null) {
            return null;
        }
        return Enum.valueOf(type, value.trim().toUpperCase());
    }

    private Object getCell(List<Object> row, String header) {
        if (row == null || header == null) {
            return null;
        }

        Integer index = headerToIndex.get(header.trim());
        if (index == null) {
            return null;
        }

        if (index < 0 || index >= row.size()) {
            return null;
        }

        return row.get(index);
    }

    private static Map<String, Integer> buildHeaderIndex(List<Object> headerRow) {
        Map<String, Integer> headerToIndex = new HashMap<>();
        if (headerRow == null) {
            return headerToIndex;
        }

        for (int i = 0; i < headerRow.size(); i++) {
            String header = String.valueOf(headerRow.get(i)).trim();
            if (header.isBlank()) {
                continue;
            }
            headerToIndex.put(header, i);
        }

        return headerToIndex;
    }

    private static List<List<Object>> extractDataRows(List<List<Object>> tableValues) {
        if (tableValues.size() <= FIRST_DATA_ROW_INDEX) {
            return List.of();
        }
        return tableValues.subList(FIRST_DATA_ROW_INDEX, tableValues.size());
    }
}
