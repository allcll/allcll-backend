package kr.allcll.backend.domain.graduation.check.result;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GraduationCheckCategoryResultRepository extends JpaRepository<GraduationCheckCategoryResult, Long> {

    List<GraduationCheckCategoryResult> findAllByUserId(Long userId);

}
