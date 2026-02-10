package kr.allcll.backend.domain.graduation.credit;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequiredCourseRepository extends JpaRepository<RequiredCourse, Long> {

    List<RequiredCourse> findByAdmissionYearAndDeptNmAndRequired(
        Integer admissionYear,
        String deptNm,
        Boolean required
    );
}
