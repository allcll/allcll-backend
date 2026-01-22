package kr.allcll.backend.domain.period;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kr.allcll.backend.domain.period.dto.PeriodsResponse;
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

    public PeriodsResponse findAll() {
        Semester semester = Semester.fromValue(Semester.now());
        List<Period> result = fetchLatestPeriodsForCurrentSemester(semester);
        return PeriodsResponse.from(result);
    }

    private List<Period> fetchLatestPeriodsForCurrentSemester(Semester semester) {
        List<Period> allPeriods = periodRepository.findAllBySemesterOrderByServiceTypeAndStartDateDesc(semester);

        Map<ServiceType, Period> latestByType = new LinkedHashMap<>();
        for (Period period : allPeriods) {
            if (!latestByType.containsKey(period.getServiceType())) {
                latestByType.put(period.getServiceType(), period);
            }
        }
        return latestByType.values().stream().toList();
    }
}
