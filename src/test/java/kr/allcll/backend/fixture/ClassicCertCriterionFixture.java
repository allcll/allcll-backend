package kr.allcll.backend.fixture;

import kr.allcll.backend.domain.graduation.certification.ClassicCertCriterion;

public class ClassicCertCriterionFixture {

    public static ClassicCertCriterion createClassicCertCriterion(int admissionYear) {
        return new ClassicCertCriterion(
            admissionYear,
            admissionYear % 100,
            10,
            4,
            2,
            3,
            1,
            ""
        );
    }
}
