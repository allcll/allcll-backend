package kr.allcll.backend.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.login")
public record LoginProperties(
    String portalLoginUrl,
    String toscLoginUrl,
    String portalLoginRedirectUrl,
    String portalLoginReferer,
    String studentInfoPageUrl,
    String englishInfoPageUrl,
    String codingInfoPageUrl
) {

}
