package kr.allcll.seatfinder.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external")
public record ExternalProperties(
    String host,
    String connectionPath,
    String nonMajorPath,
    String pinPath
) {

}
