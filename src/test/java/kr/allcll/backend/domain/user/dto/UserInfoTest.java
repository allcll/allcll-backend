package kr.allcll.backend.domain.user.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class UserInfoTest {

    @ParameterizedTest
    @ValueSource(strings = {"연기예술", "연출제작"})
    @DisplayName("영화예술학과의 세부 전공명은 영화예술학과로 정규화한다.")
    void normalizeFilmArtDepartment(String deptNm) {
        // given
        String studentId = "22012731";
        String name = "홍길동";

        // when
        UserInfo userInfo = UserInfo.of(studentId, name, deptNm);

        // then
        assertThat(userInfo.deptNm()).isEqualTo("영화예술학과");
    }

    @ParameterizedTest
    @ValueSource(strings = {"서양화", "한국화"})
    @DisplayName("회화과의 세부 전공명은 회화과로 정규화한다.")
    void normalizePaintingDepartment(String deptNm) {
        // given
        String studentId = "22012731";
        String name = "홍길동";

        // when
        UserInfo userInfo = UserInfo.of(studentId, name, deptNm);

        // then
        assertThat(userInfo.deptNm()).isEqualTo("회화과");
    }

    @ParameterizedTest
    @CsvSource({
        "성악, 음악과",
        "피아노, 음악과",
        "플루트, 음악과",
        "클라리넷, 음악과",
        "바이올린, 음악과",
        "비올라, 음악과",
        "첼로, 음악과",
        "골프, 체육학과",
        "태권도, 체육학과",
        "축구, 체육학과",
        "리듬체조, 체육학과",
        "에어로빅체조, 체육학과",
        "사격, 체육학과",
        "수영, 체육학과",
        "양궁, 체육학과",
        "발레, 무용과",
        "한국무용, 무용과",
        "현대무용, 무용과"
    })
    @DisplayName("세부 전공명은 상위 학과명으로 정규화한다.")
    void normalizeDepartment(String deptNm, String expectedDeptNm) {
        // given
        String studentId = "22012731";
        String name = "홍길동";

        // when
        UserInfo userInfo = UserInfo.of(studentId, name, deptNm);

        // then
        assertThat(userInfo.deptNm()).isEqualTo(expectedDeptNm);
    }

    @Test
    @DisplayName("일반 학과명은 변경하지 않는다.")
    void preserveDepartmentName() {
        // given
        String deptNm = "컴퓨터공학과";

        // when
        UserInfo userInfo = UserInfo.of("22012731", "홍길동", deptNm);

        // then
        assertThat(userInfo.deptNm()).isEqualTo(deptNm);
    }
}
