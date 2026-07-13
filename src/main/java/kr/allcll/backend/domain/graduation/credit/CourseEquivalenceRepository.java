package kr.allcll.backend.domain.graduation.credit;

import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseEquivalenceRepository extends JpaRepository<CourseEquivalence, Long> {

    @Query("""
        select distinct e.curiNo from CourseEquivalence e
        join CourseEquivalence e2 on e.sameCourseCode = e2.sameCourseCode
        where e2.curiNo in :curiNos
    """)
    List<String> findSameGroupCuriNos(Set<String> curiNos);

    @Query("""
        select distinct e.sameCourseCode from CourseEquivalence e
        where e.curiNo = :curiNo
    """)
    List<String> findSameCourseCodesByCuriNo(String curiNo);
}
