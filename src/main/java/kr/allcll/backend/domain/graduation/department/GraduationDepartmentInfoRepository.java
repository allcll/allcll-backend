package kr.allcll.backend.domain.graduation.department;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GraduationDepartmentInfoRepository extends JpaRepository<GraduationDepartmentInfo, Long> {

    @Query("SELECT g FROM GraduationDepartmentInfo g WHERE g.admissionYear = :admissionYear AND g.deptNm = :deptNm")
    Optional<GraduationDepartmentInfo> findByAdmissionYearAndDeptNm(int admissionYear, String deptNm);
}
