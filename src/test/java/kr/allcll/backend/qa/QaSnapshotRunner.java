package kr.allcll.backend.qa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
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
 * A2: 일괄 재검사 러너 — 학번 목록의 현재 판정을 diff 가능한 JSON 으로 dump.
 *
 * 실행 (로컬 DB 대상):
 *   ./gradlew qaTool --tests "kr.allcll.backend.qa.QaSnapshotRunner" \
 *     -Dqa.ids=ALL -Dqa.label=before
 *   -Dqa.ids: "ALL" 또는 "22010137,23012154" (studentId 콤마 목록)
 *   -Dqa.label: 스냅샷 라벨 (출력: qa-snapshots/<label>/<studentId>.json)
 *
 * 주의: GraduationChecker.calculate 는 cert 결과를 DB 에 쓰므로 @Transactional 롤백으로 감싼다.
 * 수정 전 스냅샷(before) → 코드 수정 → 스냅샷(after) → tools/qa/snapshot_diff.py 로 비교.
 */
@SpringBootTest
@Tag("qa-tool")
class QaSnapshotRunner {

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
    void snapshot() throws Exception {
        String dsUrl = env.getProperty("spring.datasource.url", "");
        if (dsUrl.contains("h2:mem")) {
            fail("qaTool 이 H2 인메모리 DB 로 실행됨 — 실 데이터가 없다. -Dspring.profiles.active=qa 로 실행하세요 (application-qa.yml 참조)");
        }
        String ids = System.getProperty("qa.ids", "ALL");
        String label = System.getProperty("qa.label", "snapshot");
        Path out = Path.of(System.getProperty("qa.out", "qa-snapshots/" + label));
        Files.createDirectories(out);

        List<User> users = "ALL".equalsIgnoreCase(ids)
            ? userRepository.findAll()
            : Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(id -> userRepository.findByStudentId(id)
                    .orElseThrow(() -> new IllegalArgumentException("학번 없음: " + id)))
                .toList();

        ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        int ok = 0, skipped = 0, failed = 0;
        for (User user : users) {
            List<CompletedCourse> courses = completedCoursePersistenceService.getCompletedCourses(user.getId());
            if (courses.isEmpty()) {
                skipped++; // 성적 미업로드 사용자
                continue;
            }
            try {
                CheckResult result = graduationChecker.calculate(user.getId(), courses);
                om.writeValue(out.resolve(user.getStudentId() + ".json").toFile(),
                    QaJson.snapshot(user, result));
                ok++;
            } catch (Exception e) {
                failed++;
                System.err.printf("[qa-snapshot] FAIL %s (%s): %s%n",
                    user.getStudentId(), user.getDeptNm(), e);
            }
        }
        System.out.printf("[qa-snapshot] label=%s → %s | 성공 %d / 미업로드 skip %d / 실패 %d%n",
            label, out.toAbsolutePath(), ok, skipped, failed);
    }
}
