package kr.allcll.backend.domain.graduation.check.result;

import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResultRepository;
import kr.allcll.backend.domain.graduation.check.result.dto.UpdateEnglishCertRequest;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GraduationEnglishCertPassUpdateService {

    private final GraduationCheckCertResultRepository graduationCheckCertResultRepository;
    private final GraduationCheckRepository graduationCheckRepository;
    private final GraduationCheckCategoryResultRepository graduationCheckCategoryResultRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateEnglishCertPass(Long userId, UpdateEnglishCertRequest updateEnglishCertRequest) {
        GraduationCheckCertResult graduationCertResult = graduationCheckCertResultRepository.findByUserId(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.GRADUATION_CERT_NOT_FOUND));
        graduationCertResult.updateEnglish(updateEnglishCertRequest.isPassed());
        graduationCertResult.reCalculate();
        updateGraduationAvailability(userId, graduationCertResult);
    }

    private void updateGraduationAvailability(Long userId, GraduationCheckCertResult graduationCertResult) {
        GraduationCheck graduationCheck = graduationCheckRepository.findByUserId(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.GRADUATION_CHECK_NOT_FOUND));

        boolean allCategoriesSatisfied = graduationCheckCategoryResultRepository.findAllByUserId(userId)
            .stream()
            .allMatch(GraduationCheckCategoryResult::getIsSatisfied);

        graduationCheck.update(allCategoriesSatisfied && graduationCertResult.getIsSatisfied());
    }
}
