package kr.allcll.backend.admin.session.dto;

/**
 * @param userId 수립된 세션의 사용자 식별자. 어드민 화면이 이후 세션 갱신 시작과 크롤링 요청에 그대로 사용한다.
 */
public record SsoLoginResponse(String userId) {

}
