package kr.allcll.backend.support.sheet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 졸업요건 검증용 - 구글 시트 스냅샷 덤프 유틸리티.
 *
 * <p>방향 2(기준 데이터 검증)의 1단계다. google.sheets.tabs 에 설정된 모든 탭을
 * CSV 로 떨궈 {@code docs/graduation/verification/snapshot/} 에 저장한다.
 * 수강편람 PDF 와 시트 데이터가 한 폴더에 모이면 AI 가 manifest.md 기준으로 둘을 대조할 수 있다.
 *
 * <p><b>CI 자동 실행 대상이 아니다</b> ({@code @Disabled}). 수동 실행 전용:
 * <pre>
 *   1. 아래 @Disabled 한 줄을 잠시 주석 처리
 *   2. ./gradlew :test --tests "*GraduationSheetDumpTask*"
 *   3. 끝나면 @Disabled 복구
 * </pre>
 *
 * <p>application-local.yml 의 google.sheets 자격 증명이 필요하다.
 * local 프로파일로 부팅하므로 로컬 MySQL(allcll_local) 도 떠 있어야 한다.
 * 시트를 읽기만 하며 DB 나 시트에 쓰지 않는다.
 */
@SpringBootTest(properties = "app.runner.enabled=false")
@ActiveProfiles("local")
@Disabled("수동 실행 전용 - 졸업요건 시트 검증 스냅샷 덤프")
class GraduationSheetDumpTask {

    private static final Path SNAPSHOT_DIR = Path.of("docs/graduation/verification/snapshot");

    @Autowired
    private GraduationSheetFetcher graduationSheetFetcher;

    @Autowired
    private GraduationSheetProperties graduationSheetProperties;

    @Test
    void dumpAllTabsToCsv() throws IOException {
        Files.createDirectories(SNAPSHOT_DIR);

        for (Map.Entry<String, String> tab : graduationSheetProperties.tabs().entrySet()) {
            String tabKey = tab.getKey();
            String tabName = tab.getValue();

            GraduationSheetTable table = graduationSheetFetcher.fetchAsTable(tabName);
            Path csvPath = SNAPSHOT_DIR.resolve(tabKey + ".csv");
            Files.writeString(csvPath, toCsv(table));

            System.out.printf("[시트 덤프] %s -> %s (%d행)%n",
                tabName, csvPath, table.getDataRows().size());
        }
    }

    private String toCsv(GraduationSheetTable table) {
        List<Map.Entry<String, Integer>> orderedColumns = table.getHeaderToIndex().entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .toList();
        List<String> headers = orderedColumns.stream().map(Map.Entry::getKey).toList();
        List<Integer> columnIndexes = orderedColumns.stream().map(Map.Entry::getValue).toList();

        StringBuilder csv = new StringBuilder();
        csv.append(toCsvRow(headers));
        for (List<Object> row : table.getDataRows()) {
            csv.append(toCsvRow(cellsInColumnOrder(row, columnIndexes)));
        }
        return csv.toString();
    }

    private List<String> cellsInColumnOrder(List<Object> row, List<Integer> columnIndexes) {
        List<String> cells = new ArrayList<>();
        for (int columnIndex : columnIndexes) {
            Object cell = columnIndex < row.size() ? row.get(columnIndex) : null;
            cells.add(cell == null ? "" : String.valueOf(cell));
        }
        return cells;
    }

    private String toCsvRow(List<String> cells) {
        return cells.stream().map(this::escape).collect(Collectors.joining(",")) + "\n";
    }

    private String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
