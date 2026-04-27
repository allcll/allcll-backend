package kr.allcll.backend.admin.session.dto;

import kr.allcll.crawler.credential.Credential;

public record SetCredentialRequest(
    String tokenJ,
    String tokenU,
    String tokenR,
    String tokenL
) {

    public boolean isValid() {
        return !isBlank(tokenJ) && !isBlank(tokenU) && !isBlank(tokenR) && !isBlank(tokenL);
    }

    public Credential toCredential() {
        return Credential.of(tokenJ, tokenU, tokenR, tokenL);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
