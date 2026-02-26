package kr.allcll.backend.fixture;

import kr.allcll.backend.domain.graduation.certification.GraduationCertRuleType;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;
import kr.allcll.backend.domain.user.User;

public class GraduationCheckCertResultFixture {

    public static GraduationCheckCertResult createCertResult(
        User user,
        GraduationCertRuleType ruleType,
        boolean englishPassed,
        boolean codingPassed,
        boolean classicPassed
    ) {
        int passedCount = ruleType.calculatePassedCount(englishPassed, classicPassed, codingPassed);
        boolean satisfied = ruleType.isSatisfied(passedCount);

        return new GraduationCheckCertResult(
            user,
            ruleType,
            passedCount,
            ruleType.getRequiredPassCount(),
            satisfied,
            englishPassed,
            codingPassed,
            classicPassed,
            0,
            0,
            0,
            0,
            false,
            0,
            0,
            false,
            0,
            0,
            false,
            0,
            0,
            false
        );
    }
}
