package kr.allcll.backend.session.dto;

import kr.allcll.crawler.credential.Credential;

public record SetCredentialRequest(
    String tokenJ,
    String tokenU,
    String tokenR,
    String tokenL
) {

    public Credential toCredential() {
        return Credential.of(tokenJ, tokenU, tokenR, tokenL);
    }
}
