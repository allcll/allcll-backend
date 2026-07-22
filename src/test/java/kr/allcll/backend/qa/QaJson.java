package kr.allcll.backend.qa;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.check.result.dto.CheckResult;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCategory;
import kr.allcll.backend.domain.user.User;

/**
 * QA 스냅샷 직렬화 — diff 가능하도록 결정적(deterministic) 구조를 강제한다.
 * 규칙: 카테고리는 "scope:type" 키로 정렬(TreeMap), 영역은 이름순 정렬, 타임스탬프 미포함.
 */
public final class QaJson {

    private static final ObjectMapper OM = new ObjectMapper();

    private QaJson() {
    }

    /** 판정 스냅샷 (사용자 메타 + 검사 결과) */
    public static Map<String, Object> snapshot(User user, CheckResult result) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("studentId", user.getStudentId());
        root.put("admissionYear", user.getAdmissionYear());
        root.put("majorType", String.valueOf(user.getMajorType()));
        root.put("collegeNm", user.getCollegeNm());
        root.put("deptNm", user.getDeptNm());
        root.put("doubleDeptNm", user.getDoubleDeptNm());
        root.put("result", resultMap(result));
        return root;
    }

    public static Map<String, Object> resultMap(CheckResult r) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("isGraduatable", r.isGraduatable());
        res.put("totalCredits", r.totalCredits());
        res.put("requiredTotalCredits", r.requiredTotalCredits());
        res.put("remainingCredits", r.remainingCredits());
        Map<String, Object> cats = new TreeMap<>();
        if (r.categories() != null) {
            for (GraduationCategory c : r.categories()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("earnedCredits", c.earnedCredits());
                m.put("requiredCredits", c.requiredCredits());
                m.put("remainingCredits", c.remainingCredits());
                if (c.earnedAreasCnt() != null || c.requiredAreasCnt() != null) {
                    m.put("earnedAreasCnt", c.earnedAreasCnt());
                    m.put("requiredAreasCnt", c.requiredAreasCnt());
                    m.put("earnedAreas", c.earnedAreas() == null
                        ? List.of()
                        : c.earnedAreas().stream().map(Enum::name).sorted().toList());
                }
                m.put("satisfied", c.satisfied());
                cats.put(c.majorScope() + ":" + c.categoryType(), m);
            }
        }
        res.put("categories", cats);
        res.put("cert", r.certResult() == null
            ? null
            : OM.convertValue(r.certResult(), new TypeReference<LinkedHashMap<String, Object>>() {
            }));
        return res;
    }

    /** qa-cases 용 입력(이수과목) 직렬화 — DB 유실 대비 아카이브 */
    public static List<Map<String, Object>> courses(List<CompletedCourse> courses) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CompletedCourse c : courses) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("curiNo", c.getCuriNo());
            m.put("curiNm", c.getCuriNm());
            m.put("categoryType", String.valueOf(c.getCategoryType()));
            m.put("selectedArea", c.getSelectedArea());
            m.put("credits", c.getCredits());
            m.put("grade", c.getGrade());
            m.put("majorScope", String.valueOf(c.getMajorScope()));
            m.put("isEarned", c.isEarned());
            list.add(m);
        }
        list.sort((a, b) -> String.valueOf(a.get("curiNo")).compareTo(String.valueOf(b.get("curiNo"))));
        return list;
    }

    /**
     * dotted-path 조회: 예) "result.categories.PRIMARY:BALANCE_REQUIRED.earnedAreasCnt"
     * (키 구분자는 '.' — 카테고리 키의 ':' 는 그대로 유지)
     */
    @SuppressWarnings("unchecked")
    public static Object resolve(Map<String, Object> root, String path) {
        Object cur = root;
        for (String part : path.split("\\.")) {
            if (!(cur instanceof Map)) {
                return null;
            }
            cur = ((Map<String, Object>) cur).get(part);
        }
        return cur;
    }

    /** 기대값 비교 — 숫자는 1e-9 허용오차, 나머지는 문자열 동등 */
    public static boolean matches(Object actual, Object expected) {
        if (actual == null || expected == null) {
            return actual == expected;
        }
        if (actual instanceof Number a && expected instanceof Number e) {
            return Math.abs(a.doubleValue() - e.doubleValue()) < 1e-9;
        }
        return String.valueOf(actual).equals(String.valueOf(expected));
    }
}
