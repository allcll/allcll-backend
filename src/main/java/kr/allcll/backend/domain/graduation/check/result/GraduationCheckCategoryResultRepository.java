package kr.allcll.backend.domain.graduation.check.result;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GraduationCheckCategoryResultRepository extends JpaRepository<GraduationCheckCategoryResult, Long> {

    @Query("""
        select r
        from GraduationCheckCategoryResult r
        where r.userId = :userId
    """)
    List<GraduationCheckCategoryResult> findAllByUserId(Long userId);
}
