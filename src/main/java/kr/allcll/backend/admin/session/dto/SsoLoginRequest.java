package kr.allcll.backend.admin.session.dto;

public record SsoLoginRequest(
    String studentId,
    String password
) {

    public boolean hasCredential() {
        return studentId != null && !studentId.isBlank()
            && password != null && !password.isBlank();
    }

    /**
     * record 가 자동으로 만드는 toString 은 모든 필드를 그대로 출력한다. 이 객체가 예외 메시지나 로그, Sentry 브레드크럼에 실리면 비밀번호가 평문으로 남으므로 직접 가린다.
     */
    @Override
    public String toString() {
        return "SsoLoginRequest{studentId=%s, password=[redacted]}".formatted(studentId);
    }
}
