package kr.allcll.backend.admin.session.sso;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SjptCrawlerProgramTest {

    @Test
    @DisplayName("중복 사용 방지가 걸린 여석만 강제 로그인으로 권한을 등록한다.")
    void forceLoginOnlyForSeat() {
        // when & then
        assertThat(SjptCrawlerProgram.SEAT.isForceLogin()).isTrue();
        assertThat(SjptCrawlerProgram.BASKET.isForceLogin()).isFalse();
        assertThat(SjptCrawlerProgram.SUBJECT.isForceLogin()).isFalse();
    }

    @Test
    @DisplayName("프로그램 키는 서로 겹치지 않는다.")
    void haveDistinctProgramKeys() {
        // when
        long distinct = Arrays.stream(SjptCrawlerProgram.values())
            .map(SjptCrawlerProgram::getProgramKey)
            .distinct()
            .count();

        // then
        assertThat(distinct).isEqualTo(SjptCrawlerProgram.values().length);
    }

    @Test
    @DisplayName("크롤러가 쓰는 프로그램은 모두 학사 시스템의 수강신청 메뉴에 속한다.")
    void belongToCourseRegistrationMenu() {
        // when & then
        assertThat(SjptCrawlerProgram.values())
            .allSatisfy(program -> assertThat(program.getProgramKey())
                .startsWith("SELF_STUDSELF_SUB_30SELF_MENU_10"));
    }
}
