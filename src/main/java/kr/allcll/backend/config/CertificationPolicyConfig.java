package kr.allcll.backend.config;

import kr.allcll.backend.domain.graduation.certification.CodingAltCoursePolicy;
import kr.allcll.backend.domain.graduation.certification.CodingCertCriterionRepository;
import kr.allcll.backend.domain.graduation.certification.EnglishAltCoursePolicy;
import kr.allcll.backend.domain.graduation.certification.EnglishCertCriterionRepository;
import kr.allcll.backend.domain.graduation.certification.GraduationCertificationAltCoursePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CertificationPolicyConfig {

    private final EnglishCertCriterionRepository englishCertCriterionRepository;
    private final CodingCertCriterionRepository codingCertCriterionRepository;

    @Bean
    public GraduationCertificationAltCoursePolicy englishAltCoursePolicy() {
        return new EnglishAltCoursePolicy(englishCertCriterionRepository);
    }

    @Bean
    public GraduationCertificationAltCoursePolicy codingAltCoursePolicy() {
        return new CodingAltCoursePolicy(codingCertCriterionRepository);
    }
}
