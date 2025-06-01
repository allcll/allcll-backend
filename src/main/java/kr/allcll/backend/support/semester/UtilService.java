package kr.allcll.backend.support.semester;

import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UtilService {

    private final Clock clock;

    public SemesterResponse getSemester() {
        LocalDate now = LocalDate.now(clock);
        Semester currentSemester = Semester.getCode(now);
        return SemesterResponse.from(currentSemester);
    }

}
