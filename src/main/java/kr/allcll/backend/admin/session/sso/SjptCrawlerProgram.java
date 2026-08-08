package kr.allcll.backend.admin.session.sso;

/**
 * 크롤러가 사용하는 수강신청 시스템 프로그램. 세션 수립 시 여기 있는 프로그램의 권한을 등록해두면, 세션이 살아 있는 동안 크롤러의 조회 네 종류가 모두 동작한다.
 */
public enum SjptCrawlerProgram {

    /**
     * 여석. 이 프로그램만 중복 사용 방지가 걸려 있어, 같은 계정으로 두 번째로 열면 먼저 열린 쪽이 자동 종료된다. 재로그인이나 복구 시 남아 있는 세션을 밀어내야 하므로 강제 로그인으로 등록한다.
     */
    SEAT("SELF_STUDSELF_SUB_30SELF_MENU_10SueReqLesnEGuide", true),

    /**
     * 관심과목. 조회 계열이라 중복 사용 방지가 없다.
     */
    BASKET("SELF_STUDSELF_SUB_30SELF_MENU_10SueReqLesnBasketGuide", false),

    /**
     * 과목·학과 조회. 조회 계열이라 중복 사용 방지가 없다.
     */
    SUBJECT("SELF_STUDSELF_SUB_30SELF_MENU_10SueOpenTimeQ", false),
    ;

    private final String programKey;
    private final boolean forceLogin;

    SjptCrawlerProgram(String programKey, boolean forceLogin) {
        this.programKey = programKey;
        this.forceLogin = forceLogin;
    }

    public String getProgramKey() {
        return programKey;
    }

    public boolean isForceLogin() {
        return forceLogin;
    }
}
