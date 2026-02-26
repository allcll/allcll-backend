package kr.allcll.backend.domain.graduation.credit;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RequiredCourseRepository extends JpaRepository<RequiredCourse, Long> {

    @Query("""
            select r from RequiredCourse r
            where r.admissionYear = :admissionYear
            and r.deptCd in :deptCds
        """)
    List<RequiredCourse> findByAdmissionYearAndDepts(Integer admissionYear, List<String> deptCds);

    @Query("""
            select r from RequiredCourse r
            where r.deptNm in :deptNms
            and r.admissionYear = :admissionYear
            and r.categoryType = :categoryType
        """)
    List<RequiredCourse> findRequiredCourses(List<String> deptNms, Integer admissionYear, CategoryType categoryType);
}
