package kr.allcll.backend.domain.graduation.check.result;

import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResultRepository;
import kr.allcll.backend.domain.graduation.check.result.dto.CertResult;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CertificationChecker {

    private final GraduationCheckCertResultRepository graduationCheckCertResultRepository;

    public CertResult getResult(Long userId) {
        GraduationCheckCertResult certResult = graduationCheckCertResultRepository.findByUserId(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.GRADUATION_CERT_NOT_FOUND));
        return CertResult.from(certResult);
    }
}
