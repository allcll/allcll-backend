package kr.allcll.backend.domain.subject.subjectReport;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import kr.allcll.crawler.subject.CrawlerSubject;
import kr.allcll.crawler.subject.CrawlerSubjectDiffResult;
import org.springframework.stereotype.Component;

@Component
public class ReportTableBuilder {

    private String getTableStyle() {
        return "style='border-collapse:collapse; margin-left:0; width:80%;'";
    }

    private String getThStyle() {
        return "style='background-color:#f2f2f2; padding:8px; border:1px solid #ddd; text-align:center;'";
    }

    private String getTdStyle() {
        return "style='padding:8px; border:1px solid #ddd; text-align:center;'";
    }

    public String buildSubjectChangeSummaryTable(int totalSubjectsCount, List<CrawlerSubject> insertSubjects,
        List<CrawlerSubject> deleteSubjects, List<CrawlerSubject> updateSubjects) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table ").append(getTableStyle()).append(">");
        sb.append("<tr>")
            .append("<th ").append(getThStyle()).append(">구분</th>")
            .append("<th ").append(getThStyle()).append(">건수</th>")
            .append("<th ").append(getThStyle()).append(">비고</th>")
            .append("</tr>");

        sb.append(buildSummaryRow("전체 과목 수", String.valueOf(totalSubjectsCount), "현재 동기화 기준 전체 과목 수"));
        sb.append(buildSummaryRow("신규 추가 과목 수", String.valueOf(insertSubjects.size()), "DB에 없던 과목이 새로 추가됨"));
        sb.append(buildSummaryRow("폐강 과목 수", String.valueOf(deleteSubjects.size()), "DB에서 isDeleted = true 로 처리됨"));
        sb.append(buildSummaryRow("정보 변경 과목 수", String.valueOf(updateSubjects.size()), "과목명, 교수명, 강의실 등 주요 정보 수정"));

        sb.append("</table>");
        return sb.toString();
    }

    private String buildSummaryRow(String category, String count, String note) {
        return "<tr>"
            + "<td " + getTdStyle() + ">" + category + "</td>"
            + "<td " + getTdStyle() + ">" + count + "</td>"
            + "<td " + getTdStyle() + ">" + note + "</td>"
            + "</tr>";
    }

    public String buildSubjectInfoTable(List<CrawlerSubject> subjects) {
        if (subjects == null || subjects.isEmpty()) {
            return "<p style='color:red;'>❌ 해당 데이터 없음 ❌</p>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<table ").append(getTableStyle()).append(">");
        sb.append("<tr>")
            .append("<th ").append(getThStyle()).append(">과목번호</th>")
            .append("<th ").append(getThStyle()).append(">과목명</th>")
            .append("<th ").append(getThStyle()).append(">강의실</th>")
            .append("<th ").append(getThStyle()).append(">시간</th>")
            .append("<th ").append(getThStyle()).append(">교수명</th>")
            .append("</tr>");

        for (CrawlerSubject subject : subjects) {
            sb.append("<tr>")
                .append("<td ").append(getTdStyle()).append(">").append(subject.getCuriNo()).append("</td>")
                .append("<td ").append(getTdStyle()).append(">").append(subject.getCuriNm()).append("</td>")
                .append("<td ").append(getTdStyle()).append(">").append(subject.getLesnRoom()).append("</td>")
                .append("<td ").append(getTdStyle()).append(">").append(subject.getLesnTime()).append("</td>")
                .append("<td ").append(getTdStyle()).append(">").append(subject.getLesnEmp()).append("</td>")
                .append("</tr>");
        }

        sb.append("</table>");
        return sb.toString();
    }

    public String buildSubjectDiffTable(List<CrawlerSubjectDiffResult> diffs) {
        if (diffs == null || diffs.isEmpty()) {
            return "<p style='color:red;'>❌ 해당 데이터 없음 ❌</p>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<table ").append(getTableStyle()).append(">");
        sb.append("<tr>")
            .append("<th ").append(getThStyle()).append(">id</th>")
            .append("<th ").append(getThStyle()).append(">과목번호</th>")
            .append("<th ").append(getThStyle()).append(">과목명</th>")
            .append("<th ").append(getThStyle()).append(">강의실 변경</th>")
            .append("<th ").append(getThStyle()).append(">시간 변경</th>")
            .append("<th ").append(getThStyle()).append(">교수명 변경</th>")
            .append("</tr>");

        for (CrawlerSubjectDiffResult diff : diffs) {
            sb.append("<tr>")
                .append("<td ").append(getTdStyle()).append(">").append(diff.id()).append("</td>")
                .append("<td ").append(getTdStyle()).append(">").append(diff.curiNo()).append("</td>")
                .append("<td ").append(getTdStyle()).append(">").append(diff.curiNm()).append("</td>")
                .append("<td ").append(getTdStyle()).append(">").append(diff.lesnRoom()).append("</td>")
                .append("<td ").append(getTdStyle()).append(">").append(diff.lesnTime()).append("</td>")
                .append("<td ").append(getTdStyle()).append(">").append(diff.lesnEmp()).append("</td>")
                .append("</tr>");
        }

        sb.append("</table>");
        return sb.toString();
    }

    public String buildSyncMetadataTable(CrawlingMetaData metaData) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table ").append(getTableStyle()).append(">");
        sb.append("<tr>")
            .append("<th ").append(getThStyle()).append(">항목</th>")
            .append("<th ").append(getThStyle()).append(">내용</th>")
            .append("</tr>");

        sb.append(buildSummaryRow("과목 동기화 실행 시각", formatDateTime(metaData.crawlingStartTime())));
        sb.append(buildSummaryRow("과목 동기화 종료 시각", formatDateTime(metaData.crawlingEndTime())));
        sb.append(buildSummaryRow("동기화 소요 시간", metaData.formattedDuration()));

        sb.append("</table>");
        return sb.toString();
    }

    private String buildSummaryRow(String category, String time) {
        return "<tr>"
            + "<td " + getTdStyle() + ">" + category + "</td>"
            + "<td " + getTdStyle() + ">" + time + "</td>"
            + "</tr>";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}
