package kr.allcll.backend.domain.graduation.department;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GraduationDepartmentInfoRepository extends JpaRepository<GraduationDepartmentInfo, Long> {

    @Query("""
        select g FROM GraduationDepartmentInfo g
        where g.admissionYear = :admissionYear
        and g.deptNm = :deptNm
        """)
    Optional<GraduationDepartmentInfo> findByAdmissionYearAndDeptNm(int admissionYear, String deptNm);

    @Query("""
        select g from GraduationDepartmentInfo g
        where g.admissionYear = :admissionYear
        and g.deptCd = :deptCd
        """)
    Optional<GraduationDepartmentInfo> findByAdmissionYearAndDeptCd(int admissionYear, String deptCd);

    @Query("""
        select g from GraduationDepartmentInfo g
        where g.admissionYear = :admissionYear
        """)
    List<GraduationDepartmentInfo> findAllByAdmissionYear(Integer admissionYear);

    @Query("""
            select d from GraduationDepartmentInfo d
            where d.admissionYear = :admissionYear
            and d.deptCd = :deptCd
        """)
    Optional<GraduationDepartmentInfo> findByAdmissionYearAndDeptCd(Integer admissionYear, String deptCd);
}
