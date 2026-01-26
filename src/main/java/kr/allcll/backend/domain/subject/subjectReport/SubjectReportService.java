package kr.allcll.backend.domain.subject.subjectReport;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import kr.allcll.backend.admin.subject.SubjectDiffResult;
import kr.allcll.backend.admin.subject.SubjectSyncResult;
import kr.allcll.backend.client.EmailProperties;
import kr.allcll.backend.support.semester.Semester;
import kr.allcll.crawler.subject.CrawlerSubject;
import kr.allcll.crawler.subject.CrawlerSubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectReportService {

    private final JavaMailSender emailSender;
    private final ReportTableBuilder reportTableBuilder;
    private final CrawlerSubjectRepository crawlerSubjectRepository;
    private final String EMAIL_ADDRESS = "allclllclla@gmail.com";
    private final EmailProperties emailProperties;

    public void generateSubjectSyncReport(SubjectSyncResult syncResult, CrawlingMetaData metaData) {
        List<CrawlerSubject> allSubjects = crawlerSubjectRepository.findAllBySemesterAt(Semester.getCurrentSemester());
        int totalSubjectsCount = allSubjects.size();

        sendSubjectReportToEmail(syncResult, metaData, totalSubjectsCount);
    }

    private void sendSubjectReportToEmail(SubjectSyncResult subjectSyncResult, CrawlingMetaData metaData,
        int totalSubjectsCount) {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        List<CrawlerSubject> insertSubjectReport = subjectSyncResult.subjectsToAdd();
        List<CrawlerSubject> deleteSubjectReport = subjectSyncResult.subjectsToDelete();
        List<CrawlerSubject> updateSubjectReport = subjectSyncResult.subjectsCanUpdate();
        List<SubjectDiffResult> updateSubjectDiffResults = subjectSyncResult.updateDiffs();

        String title = "[ALLCLL] 과목 데이터 변경내역 (" + dateTime + ")";
        String content = "<html>" +
            "<body style='text-align:left; font-family:sans-serif;'>" +
            "<h2>📊 과목 변경 집계 리포트 </h2>" +
            reportTableBuilder.buildSubjectChangeSummaryTable(totalSubjectsCount, insertSubjectReport,
                deleteSubjectReport, updateSubjectReport) +

            "<br><hr><h2>🔎 과목 정보 변경 세부 집계 </h2>" +
            "<h3>1) 추가된 과목</h3>" +
            reportTableBuilder.buildSubjectInfoTable(insertSubjectReport) +
            "<h3>2) 삭제된 과목</h3>" +
            reportTableBuilder.buildSubjectInfoTable(deleteSubjectReport) +
            "<h3>3) 수정된 과목</h3>" +
            reportTableBuilder.buildSubjectDiffTable(updateSubjectDiffResults) +

            "<br><hr><h2>⏰ 동기화 수행 정보</h2>" +
            reportTableBuilder.buildSyncMetadataTable(metaData) +
            "</body>" +
            "</html>";
        try {
            sendMail(title, content);
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send email", e);
        }
    }

    private void sendMail(String title, String content) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(emailProperties.username());
            helper.setSubject(title);
            helper.setText(content, true);

            emailSender.send(message);
        } catch (RuntimeException e) {
            throw new RuntimeException("Unable to send email", e);
        }
    }

}
