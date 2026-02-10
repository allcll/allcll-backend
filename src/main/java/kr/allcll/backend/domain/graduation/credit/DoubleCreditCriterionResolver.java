package kr.allcll.backend.domain.graduation.credit;

import java.util.List;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DoubleCreditCriterionResolver {

    private static final String ALL_DEPT = "0";

    private final DoubleCreditCriterionRepository doubleCreditCriterionRepository;

    public List<DoubleCreditCriterion> resolve(User user) {
        if (user.getDoubleDeptCd() == null) {
            return List.of();
        }
        Integer admissionYear = user.getAdmissionYear();
        MajorType majorType = user.getMajorType();
        String primaryDeptCd = user.getDeptCd();
        String secondaryDeptCd = user.getDoubleDeptCd();

        List<DoubleCreditCriterion> bothMatchedDoubleCreditCriteria = doubleCreditCriterionRepository.findByPair(admissionYear, majorType, primaryDeptCd, secondaryDeptCd);
        if (!bothMatchedDoubleCreditCriteria.isEmpty()) {
            return bothMatchedDoubleCreditCriteria;
        }

        List<DoubleCreditCriterion> primaryDeptCdMatchedDoubleCreditCriteria = doubleCreditCriterionRepository.findByPair(admissionYear, majorType, primaryDeptCd, ALL_DEPT);
        if (!primaryDeptCdMatchedDoubleCreditCriteria.isEmpty()) return primaryDeptCdMatchedDoubleCreditCriteria;

        return doubleCreditCriterionRepository.findByPair(admissionYear, majorType, ALL_DEPT, secondaryDeptCd);
    }
}
