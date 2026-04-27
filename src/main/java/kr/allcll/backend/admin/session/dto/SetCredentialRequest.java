package kr.allcll.backend.admin.session.dto;

import jakarta.validation.constraints.NotBlank;
import kr.allcll.crawler.credential.Credential;

public record SetCredentialRequest(
    @NotBlank
    String tokenJ,

    @NotBlank
    String tokenU,

    @NotBlank
    String tokenR,

    @NotBlank
    String tokenL
) {

    public Credential toCredential() {
        return Credential.of(tokenJ, tokenU, tokenR, tokenL);
    }
}
