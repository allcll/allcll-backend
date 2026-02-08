package kr.allcll.backend.domain.graduation.department;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GraduationDepartmentInfoRepository extends JpaRepository<GraduationDepartmentInfo, Long> {

    @Query("""
        select d FROM GraduationDepartmentInfo d 
        where d.admissionYear = :admissionYear 
        and d.deptNm = :deptNm
        """)
    Optional<GraduationDepartmentInfo> findByAdmissionYearAndDeptNm(int admissionYear, String deptNm);

    @Query("""
        select g from GraduationDepartmentInfo g
        where g.admissionYear = :admissionYear
        and g.deptCd = :deptCd
        """)
    Optional<GraduationDepartmentInfo> findByAdmissionYearAndDeptCd(int admissionYear, String deptCd);
}
