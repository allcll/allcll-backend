package kr.allcll.backend.session.dto;

import kr.allcll.crawler.credential.Credential;

public record CredentialResponse(
    String tokenJ,
    String tokenU,
    String tokenR,
    String tokenL
) {

    public static CredentialResponse fromCredential(Credential credential) {
        return new CredentialResponse(
            credential.getTokenJ(),
            credential.getTokenU(),
            credential.getTokenR(),
            credential.getTokenL()
        );
    }
}
