package kr.allcll.backend.domain.basket;

import java.util.List;
import kr.allcll.backend.domain.basket.dto.SubjectTotalCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BasketRepository extends JpaRepository<Basket, Long> {

    @Query("""
        select b from Basket b
        join fetch b.subject s
        where s.id = :id
        and s.isDeleted = false
        and s.semesterAt = :semesterAt
        """)
    List<Basket> findBySubjectId(Long id, String semesterAt);

    @Query("""
        select new kr.allcll.backend.domain.basket.dto.SubjectTotalCount(s.id, max(b.totRcnt))
        from Basket b
        join b.subject s
        where s.id in :subjectIds
        and s.isDeleted = false
        and s.semesterAt = :semesterAt
        group by s.id
        """)
    List<SubjectTotalCount> findTotalCountsBySubjectIds(List<Long> subjectIds, String semesterAt);
}
