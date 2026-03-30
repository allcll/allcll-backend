package kr.allcll.backend.domain.graduation.credit;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseEquivalenceRepository extends JpaRepository<CourseEquivalence, Long> {

    @Query("""
        select e.curiNo from CourseEquivalence e
        join CourseEquivalence e2 on e.groupCode = e2.groupCode
        where e2.curiNo in :curiNos
    """)
    List<String> findSameGroupCuriNos(Set<String> curiNos);

    @Query("""
        select e.groupCode from CourseEquivalence e
        where e.curiNo = :curiNo
    """)
    Optional<String> findGroupCodeByCuriNo(String curiNo);
}
