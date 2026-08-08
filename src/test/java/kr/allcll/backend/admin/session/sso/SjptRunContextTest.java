package kr.allcll.backend.admin.session.sso;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * addParam 은 base64(urlEncode(json)) 이다. 인코딩기 자체를 인코딩기로 검증하면 순환이 되므로, 디코딩해 원래 JSON 이 나오는지와 크롤러의 인코딩 규약(콜론과 쉼표를 인코딩하지
 * 않는다)이 지켜지는지를 각각 확인한다.
 */
class SjptRunContextTest {

    private static final String USER_ID = "22011794";
    private static final String LOGIN_DT = "20260722165501";
    private static final String RUNNING_SEJONG = "6d760f74-d762-4f3d-b902-03dfe9e3ada2";
    private static final String SEAT_PROGRAM_KEY = "SELF_STUDSELF_SUB_30SELF_MENU_10SueReqLesnEGuide";

    @Test
    @DisplayName("초기화 요청용 컨텍스트는 토큰 세 개를 빈 값으로만 담는다.")
    void buildEmptyContext() {
        // when
        String json = SjptRunContext.empty().toJson();

        // then
        assertThat(json).isEqualTo("{\"_runIntgUsrNo\":\"\",\"_runPgLoginDt\":\"\",\"_runningSejong\":\"\"}");
    }

    @Test
    @DisplayName("세션 수립 후 컨텍스트는 토큰 세 개만 담고 프로그램 정보는 넣지 않는다.")
    void buildSessionContext() {
        // given
        SjptUserInfoResponse userInfo = new SjptUserInfoResponse(USER_ID, RUNNING_SEJONG, LOGIN_DT);

        // when
        String json = SjptRunContext.of(userInfo).toJson();

        // then
        assertThat(json).isEqualTo(
            "{\"_runIntgUsrNo\":\"%s\",\"_runPgLoginDt\":\"%s\",\"_runningSejong\":\"%s\"}"
                .formatted(USER_ID, LOGIN_DT, RUNNING_SEJONG)
        );
    }

    @Test
    @DisplayName("role 등록용 컨텍스트는 강제 로그인 여부와 프로그램 키를 앞쪽에 담는다.")
    void buildProgramContext() {
        // given
        SjptRunContext session = SjptRunContext.of(new SjptUserInfoResponse(USER_ID, RUNNING_SEJONG, LOGIN_DT));

        // when
        String json = session.forProgram(SEAT_PROGRAM_KEY, true).toJson();

        // then
        assertThat(json).startsWith("{\"pbForceLog\":\"true\",\"_runPgmKey\":\"%s\",\"_runSysKey\":\"SCH\","
            .formatted(SEAT_PROGRAM_KEY));
        assertThat(json).endsWith("\"_runningSejong\":\"%s\"}".formatted(RUNNING_SEJONG));
    }

    @Test
    @DisplayName("강제 로그인 여부는 JSON 불리언이 아니라 문자열로 보낸다.")
    void sendForceLoginAsString() {
        // given
        SjptRunContext session = SjptRunContext.of(new SjptUserInfoResponse(USER_ID, RUNNING_SEJONG, LOGIN_DT));

        // when
        String json = session.forProgram(SEAT_PROGRAM_KEY, false).toJson();

        // then
        assertThat(json).contains("\"pbForceLog\":\"false\"");
        assertThat(json).doesNotContain("\"pbForceLog\":false");
    }

    @Test
    @DisplayName("role 등록 JSON 이 실서버에서 동작을 확인한 형태와 정확히 일치한다.")
    void matchVerifiedProgramJson() {
        // given
        // 아래 문자열은 실계정으로 초기화-메뉴-role 등록 시퀀스를 실행해 200 응답과
        // dm_UserRole.MENU_CD 일치를 확인한 PoC RunContext.toJson() 의 출력이다.
        String verified = "{\"pbForceLog\":\"true\","
            + "\"_runPgmKey\":\"SELF_STUDSELF_SUB_30SELF_MENU_10SueReqLesnEGuide\","
            + "\"_runSysKey\":\"SCH\","
            + "\"_runIntgUsrNo\":\"22011794\","
            + "\"_runPgLoginDt\":\"20260722165501\","
            + "\"_runningSejong\":\"6d760f74-d762-4f3d-b902-03dfe9e3ada2\"}";
        SjptRunContext session = SjptRunContext.of(new SjptUserInfoResponse(USER_ID, RUNNING_SEJONG, LOGIN_DT));

        // when
        String json = session.forProgram(SEAT_PROGRAM_KEY, true).toJson();

        // then
        assertThat(json).isEqualTo(verified);
    }

    @Test
    @DisplayName("addParam 을 되돌리면 원래 JSON 이 나온다.")
    void addParamRoundTrip() {
        // given
        SjptRunContext context = SjptRunContext.of(new SjptUserInfoResponse(USER_ID, RUNNING_SEJONG, LOGIN_DT));

        // when
        String urlEncoded = new String(Base64.getDecoder().decode(context.toAddParam()), StandardCharsets.UTF_8);

        // then
        assertThat(URLDecoder.decode(urlEncoded, StandardCharsets.UTF_8)).isEqualTo(context.toJson());
    }

    @Test
    @DisplayName("크롤러 규약대로 콜론과 쉼표는 인코딩하지 않는다.")
    void keepColonAndCommaUnencoded() {
        // given
        SjptRunContext context = SjptRunContext.of(new SjptUserInfoResponse(USER_ID, RUNNING_SEJONG, LOGIN_DT));

        // when
        String urlEncoded = new String(Base64.getDecoder().decode(context.toAddParam()), StandardCharsets.UTF_8);

        // then
        assertThat(urlEncoded).contains(":").contains(",");
        assertThat(urlEncoded).doesNotContain("%3A").doesNotContain("%2C");
    }
}
