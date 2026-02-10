package kr.allcll.backend.domain.graduation.credit;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RequiredCourseRepository extends JpaRepository<RequiredCourse, Long> {

    @Query("""
        select r from RequiredCourse r
        where r.admissionYear = :admissionYear
        and r.deptCd in :deptCds
        and r.required = true
    """)
    List<RequiredCourse> findRequiredByAdmissionYearAndDeptCdIn(Integer admissionYear, List<String> deptCds);
}
