package kr.allcll.backend.admin.period;

import java.util.List;
import kr.allcll.backend.admin.period.dto.PeriodDetailRequest;
import kr.allcll.backend.admin.period.dto.PeriodRequest;
import kr.allcll.backend.domain.period.Period;
import kr.allcll.backend.domain.period.PeriodRepository;
import kr.allcll.backend.support.semester.Semester;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminPeriodService {

    private final PeriodRepository periodRepository;

    @Transactional
    public void createPeriod(Semester semesterCode, PeriodRequest periodRequest) {
        List<PeriodDetailRequest> periodDetailRequests = periodRequest.serviceInfo();
        String semesterValue = semesterCode.getValue();

        List<Period> periods = periodDetailRequests.stream()
            .map(request -> Period.create(
                semesterCode,
                semesterValue,
                request.serviceType(),
                request.startDate(),
                request.endDate(),
                request.message()
            ))
            .toList();
        periodRepository.saveAll(periods);
    }
}
