package kr.allcll.backend.domain.graduation.check.result;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GraduationCheckBalanceAreaResultRepository extends
    JpaRepository<GraduationCheckBalanceAreaResult, Long> {

    @Query("""
            select r
            from GraduationCheckBalanceAreaResult r
            where r.userId = :userId
        """)
    List<GraduationCheckBalanceAreaResult> findAllByUserId(Long userId);
}
