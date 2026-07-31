package kr.allcll.backend.domain.user.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
