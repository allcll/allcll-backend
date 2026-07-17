package kr.allcll.backend.qa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCoursePersistenceService;
import kr.allcll.backend.domain.graduation.check.result.GraduationChecker;
import kr.allcll.backend.domain.graduation.check.result.dto.CheckResult;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

/**
 * A1: 신고 학번 → qa-cases/<studentId>.json export.
 *
 * 케이스 파일 = { user 메타, input(이수과목 아카이브), baseline(현재 코드 판정), expect(비어있음) }
 * expect 는 이후 배치 작업 시 wiki 정본을 보고 채운다 (QaReplayTest 가 소비).
 *
 * 실행:
 *   ./gradlew qaTool --tests "kr.allcll.backend.qa.QaCaseExportRunner" \
 *     -Dqa.ids=22010137,23012154 [-Dqa.batch=B1-균형카운트]
 *
 * ⚠️ qa-cases/ 는 사용자 성적(개인정보) 포함 — git 커밋 금지 (.gitignore 등록됨).
 */
@SpringBootTest
@Tag("qa-tool")
class QaCaseExportRunner {

    @Autowired
    private Environment env;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompletedCoursePersistenceService completedCoursePersistenceService;
    @Autowired
    private GraduationChecker graduationChecker;

    @Test
    @Transactional
    void export() throws Exception {
        String dsUrl = env.getProperty("spring.datasource.url", "");
        if (dsUrl.contains("h2:mem")) {
            fail("qaTool 이 H2 인메모리 DB 로 실행됨 — 실 데이터가 없다. -Dspring.profiles.active=qa 로 실행하세요 (application-qa.yml 참조)");
        }
        String ids = System.getProperty("qa.ids", "");
        if (ids.isBlank()) {
            throw new IllegalArgumentException("-Dqa.ids=학번1,학번2 필수");
        }
        String batch = System.getProperty("qa.batch", "");
        Path out = Path.of(System.getProperty("qa.out", "qa-cases"));
        Files.createDirectories(out);

        ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        for (String raw : Arrays.stream(ids.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()) {
            User user = userRepository.findByStudentId(raw)
                .orElse(null);
            if (user == null) {
                System.err.printf("[qa-case] 학번 없음 (DB 미가입 or 미업로드): %s%n", raw);
                continue;
            }
            List<CompletedCourse> courses = completedCoursePersistenceService.getCompletedCourses(user.getId());
            if (courses.isEmpty()) {
                System.err.printf("[qa-case] 성적 미업로드: %s — 합성 fixture 필요%n", raw);
                continue;
            }
            CheckResult baseline = graduationChecker.calculate(user.getId(), courses);

            Map<String, Object> caseFile = new LinkedHashMap<>();
            caseFile.put("studentId", user.getStudentId());
            caseFile.put("batch", batch);
            caseFile.put("user", QaJson.snapshot(user, baseline)); // 메타 + baseline 판정 포함
            caseFile.put("input", Map.of("courses", QaJson.courses(courses)));
            // wiki 정본 보고 채울 것: [{"path":"result.categories.PRIMARY:BALANCE_REQUIRED.earnedAreasCnt","value":2,"근거":"cohort/2022/balance.md §1 (수강편람 2026-1 p.52 §4)"}]
            caseFile.put("expect", List.of());

            om.writeValue(out.resolve(user.getStudentId() + ".json").toFile(), caseFile);
            System.out.printf("[qa-case] export %s (%d 과목)%n", user.getStudentId(), courses.size());
        }
    }
}
