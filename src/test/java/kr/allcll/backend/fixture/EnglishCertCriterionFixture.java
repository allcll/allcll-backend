package kr.allcll.backend.fixture;

import kr.allcll.backend.domain.graduation.certification.*;

public class EnglishCertCriterionFixture {

    public static EnglishCertCriterion createNonMajorEnglishCertCriterion(int admissionYear) {
        return new EnglishCertCriterion(
            admissionYear,
            admissionYear % 100,
            EnglishTargetType.NON_MAJOR,
            700,
            80,
            556,
            301,
            "Intermediate Low",
            "Intermediate Low",
            2,
            65,
            0,
            "006844",
            "Intensive English",
            3,
            ""
        );
    }

    public static EnglishCertCriterion createMajorEnglishCertCriterion(int admissionYear) {
        return new EnglishCertCriterion(
            admissionYear,
            admissionYear % 100,
            EnglishTargetType.ENGLISH_MAJOR,
            800,
            91,
            637,
            348,
            "Intermediate Mid 1",
            "Intermediate Mid 1",
            2,
            77,
            0,
            "006844",
            "Intensive English",
            3,
            "note"
        );
    }
}

