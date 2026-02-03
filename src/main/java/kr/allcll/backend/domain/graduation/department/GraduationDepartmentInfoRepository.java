package kr.allcll.backend.domain.graduation.department;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GraduationDepartmentInfoRepository extends JpaRepository<GraduationDepartmentInfo, Long> {

    GraduationDepartmentInfo findByDeptNm(String s);
}
