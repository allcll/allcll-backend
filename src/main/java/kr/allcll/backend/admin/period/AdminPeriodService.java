package kr.allcll.backend.admin.period;

import java.util.Optional;
import kr.allcll.backend.admin.period.dto.PeriodRequest;
import kr.allcll.backend.domain.period.OperationPeriod;
import kr.allcll.backend.domain.period.OperationType;
import kr.allcll.backend.domain.period.PeriodRepository;
import kr.allcll.backend.support.semester.Semester;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminPeriodService {

    private final PeriodRepository periodRepository;

    @Transactional
    public void savePeriod(Semester semester, PeriodRequest periodRequest) {
        Optional<OperationPeriod> foundPeriod = periodRepository.findBySemesterAndServiceType(semester,
            periodRequest.operationType());

        if (foundPeriod.isPresent()) {
            foundPeriod.get().update(
                periodRequest.startDate(),
                periodRequest.endDate(),
                periodRequest.message()
            );
            return;
        }

        OperationPeriod newPeriod = periodRequest.toPeriod(semester);
        periodRepository.save(newPeriod);
    }

    @Transactional
    public void deletePeriod(Semester semester, OperationType operationType) {
        periodRepository.deleteBySemesterAndServiceType(semester, operationType);
    }
}
