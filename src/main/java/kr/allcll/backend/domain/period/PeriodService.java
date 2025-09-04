package kr.allcll.backend.domain.period;

import java.util.List;
import kr.allcll.backend.domain.period.dto.PeriodDetailRequest;
import kr.allcll.backend.domain.period.dto.PeriodRequest;
import kr.allcll.backend.support.semester.Semester;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PeriodService {

    private final PeriodRepository periodRepository;

    @Transactional
    public void createPeriod(Semester semesterCode, PeriodRequest periodResponses) {
        List<PeriodDetailRequest> periodDetailResponse = periodResponses.services();
        String semesterValue = semesterCode.getValue();

        for (PeriodDetailRequest periodDetailRequest : periodDetailResponse) {
            periodRepository.save(
                Period.create(
                    semesterCode,
                    semesterValue,
                    periodDetailRequest.serviceType(),
                    periodDetailRequest.startDate(),
                    periodDetailRequest.endDate(),
                    periodDetailRequest.message()
                )
            );
        }
    }

}
