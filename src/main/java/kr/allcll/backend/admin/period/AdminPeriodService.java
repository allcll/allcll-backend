package kr.allcll.backend.admin.period;

import java.util.Optional;
import kr.allcll.backend.admin.period.dto.PeriodRequest;
import kr.allcll.backend.domain.period.Period;
import kr.allcll.backend.domain.period.PeriodRepository;
import kr.allcll.backend.domain.period.ServiceType;
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
        Optional<Period> foundPeriod = periodRepository.findBySemesterAndServiceType(semester, periodRequest.serviceType());

        if (foundPeriod.isPresent()) {
            foundPeriod.get().update(
                periodRequest.startDate(),
                periodRequest.endDate(),
                periodRequest.message()
            );
            return;
        }

        Period newPeriod = periodRequest.toPeriod(semester);
        periodRepository.save(newPeriod);
    }

    @Transactional
    public void deletePeriod(Semester semester, ServiceType serviceType) {
        periodRepository.deleteBySemesterAndServiceType(semester, serviceType);
    }
}
