package kr.allcll.backend.admin.period.dto;

import java.util.List;
import kr.allcll.backend.support.semester.Semester;

public record PeriodRequest(
    Semester code,
    String semester,
    List<PeriodDetailRequest> serviceInfo
) {

}
