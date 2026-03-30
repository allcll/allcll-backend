package kr.allcll.backend.domain.graduation.credit;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseEquivalenceRepository extends JpaRepository<CourseEquivalence, Long> {

    @Query("""
        select e.groupCode from CourseEquivalence e
        where e.curiNo = :curiNo
    """)
    Optional<String> findGroupCodeByCuriNo(String curiNo);
}
