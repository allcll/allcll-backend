package kr.allcll.backend.admin.operationPeriod;

import java.util.Optional;
import kr.allcll.backend.admin.operationPeriod.dto.OperationPeriodRequest;
import kr.allcll.backend.domain.operationPeriod.OperationPeriod;
import kr.allcll.backend.domain.operationPeriod.OperationType;
import kr.allcll.backend.domain.operationPeriod.OperationPeriodRepository;
import kr.allcll.backend.support.semester.Semester;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminOperationPeriodService {

    private final OperationPeriodRepository operationPeriodRepository;

    @Transactional
    public void saveOperationPeriod(Semester semester, OperationPeriodRequest operationPeriodRequest) {
        Optional<OperationPeriod> foundPeriod = operationPeriodRepository.findBySemesterAndOperationType(semester,
            operationPeriodRequest.operationType());

        if (foundPeriod.isPresent()) {
            foundPeriod.get().update(
                operationPeriodRequest.startDate(),
                operationPeriodRequest.endDate(),
                operationPeriodRequest.message()
            );
            return;
        }

        OperationPeriod newPeriod = operationPeriodRequest.toPeriod(semester);
        operationPeriodRepository.save(newPeriod);
    }

    @Transactional
    public void deleteOperationPeriod(Semester semester, OperationType operationType) {
        operationPeriodRepository.deleteBySemesterAndOperationType(semester, operationType);
    }
}
