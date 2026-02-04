package kr.allcll.backend.domain.graduation.department;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GraduationDepartmentInfoRepository extends JpaRepository<GraduationDepartmentInfo, Long> {

    Optional<GraduationDepartmentInfo> findByDeptNm(String deptNm);
}
