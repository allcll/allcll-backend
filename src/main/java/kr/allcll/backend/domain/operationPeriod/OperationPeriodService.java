package kr.allcll.backend.domain.operationPeriod;

import java.time.LocalDate;
import java.util.List;
import kr.allcll.backend.domain.operationPeriod.dto.OperationPeriodsResponse;
import kr.allcll.backend.support.semester.Semester;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OperationPeriodService {

    private final OperationPeriodRepository operationPeriodRepository;

    public OperationPeriodsResponse findAll(LocalDate date) {
        Semester semester = Semester.findByDate(date);
        List<OperationPeriod> operationPeriods = operationPeriodRepository.findAllBySemester(semester);
        return OperationPeriodsResponse.from(operationPeriods);
    }
}
