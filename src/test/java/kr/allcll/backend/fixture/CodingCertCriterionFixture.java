package kr.allcll.backend.fixture;

import kr.allcll.backend.domain.graduation.certification.CodingCertCriterion;
import kr.allcll.backend.domain.graduation.certification.CodingTargetType;

public class CodingCertCriterionFixture {

    public static CodingCertCriterion createNonMajorCodingCertCriterion(int admissionYear) {
        return new CodingCertCriterion(
            admissionYear,
            admissionYear % 100,
            CodingTargetType.NON_MAJOR,
            5,
            "009913",
            "고급C프로그래밍및실습",
            "B0",
            "10543",
            "K-MOOC:코딩과스토리텔링",
            "P"
            ,""
        );
    }

    public static CodingCertCriterion createMajorCodingCertCriterion(int admissionYear) {
        return new CodingCertCriterion(
            admissionYear,
            admissionYear % 100,
            CodingTargetType.CODING_MAJOR,
            3,
            "009913",
            "고급C프로그래밍및실습",
            "B0",
            null,
            null,
            null,
            ""
        );
    }
}
