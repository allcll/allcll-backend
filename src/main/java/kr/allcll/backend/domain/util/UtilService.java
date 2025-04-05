package kr.allcll.backend.domain.util;

import java.time.Clock;
import java.time.LocalDate;
import kr.allcll.backend.domain.util.dto.SemesterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UtilService {

    private final Clock clock;

    public SemesterResponse getSemester() {
        LocalDate now = LocalDate.now(clock);
        SemesterCode currentSemesterCode = SemesterCode.getCode(now);
        return SemesterResponse.from(currentSemesterCode);
    }

}
