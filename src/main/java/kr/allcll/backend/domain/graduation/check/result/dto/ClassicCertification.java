package kr.allcll.backend.domain.graduation.check.result.dto;

import java.util.List;

public record ClassicCertification(
    Boolean isRequired,
    Boolean isPassed,
    Integer totalRequiredCount,
    Integer totalMyCount,
    List<ClassicDomainRequirement> domains // 고전독서 영역 별 결과
) {

}
