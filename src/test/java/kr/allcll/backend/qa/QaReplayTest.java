package kr.allcll.backend.qa;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCoursePersistenceService;
import kr.allcll.backend.domain.graduation.check.result.GraduationChecker;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * A4: 신고 케이스 리플레이 — qa-cases/*.json 중 expect 가 채워진 케이스를
 * 현재 코드로 재검사해서 기대값(wiki 정본 근거)과 대조한다.
 *
 * 실행:
 *   ./gradlew qaTool --tests "kr.allcll.backend.qa.QaReplayTest" [-Dqa.batch=B1-균형카운트]
 *   -Dqa.batch 지정 시 해당 배치 케이스만 검사.
 *
 * 게이트: 배치 수정 완료 선언 전, 그 배치의 모든 케이스가 PASS 여야 한다 (graduation-qa Step 4).
 */
@SpringBootTest
@Tag("qa-tool")
class QaReplayTest {

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
    void replay() throws Exception {
        String dsUrl = env.getProperty("spring.datasource.url", "");
        if (dsUrl.contains("h2:mem")) {
            fail("qaTool 이 H2 인메모리 DB 로 실행됨 — 실 데이터가 없다. -Dspring.profiles.active=qa 로 실행하세요 (application-qa.yml 참조)");
        }
        Path dir = Path.of(System.getProperty("qa.cases", "qa-cases"));
        String batchFilter = System.getProperty("qa.batch", "");
        if (!Files.isDirectory(dir)) {
            fail("qa-cases/ 없음 — 먼저 QaCaseExportRunner 로 케이스를 export 하세요");
        }
        ObjectMapper om = new ObjectMapper();
        List<String> failures = new ArrayList<>();
        int cases = 0, checks = 0;

        try (Stream<Path> files = Files.list(dir)) {
            for (Path f : files.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                Map<String, Object> caseFile = om.readValue(f.toFile(),
                    new TypeReference<Map<String, Object>>() {
                    });
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> expects = (List<Map<String, Object>>) caseFile.getOrDefault("expect", List.of());
                String batch = String.valueOf(caseFile.getOrDefault("batch", ""));
                if (expects.isEmpty()) {
                    continue; // 기대값 미작성 케이스는 skip
                }
                if (!batchFilter.isBlank() && !batchFilter.equals(batch)) {
                    continue;
                }
                String studentId = String.valueOf(caseFile.get("studentId"));
                User user = userRepository.findByStudentId(studentId).orElse(null);
                if (user == null) {
                    failures.add(studentId + ": DB 에 사용자 없음");
                    continue;
                }
                List<CompletedCourse> courses = completedCoursePersistenceService.getCompletedCourses(user.getId());
                Map<String, Object> actual = QaJson.snapshot(user,
                    graduationChecker.calculate(user.getId(), courses));
                cases++;
                for (Map<String, Object> exp : expects) {
                    checks++;
                    String path = String.valueOf(exp.get("path"));
                    Object want = exp.get("value");
                    Object got = QaJson.resolve(actual, path);
                    if (!QaJson.matches(got, want)) {
                        failures.add(String.format("%s [%s] %s: 기대 %s ≠ 실제 %s",
                            studentId, batch, path, want, got));
                    }
                }
            }
        }
        System.out.printf("[qa-replay] 케이스 %d / 검증 %d / 실패 %d%n", cases, checks, failures.size());
        if (!failures.isEmpty()) {
            fail("리플레이 실패 " + failures.size() + "건:\n" + String.join("\n", failures));
        }
    }
}
