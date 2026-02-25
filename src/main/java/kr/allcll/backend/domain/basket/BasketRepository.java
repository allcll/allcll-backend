package kr.allcll.backend.domain.basket;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BasketRepository extends JpaRepository<Basket, Long> {

    @Query("""
        select b from Basket b
        join fetch b.subject s
        where s.id = :id
        and s.isDeleted = false
        and s.semesterAt = :semesterAt
        """)
    List<Basket> findBySubjectId(Long id, String semesterAt);

    // #1 N+1 제거: 여러 과목의 Basket을 한 번에 조회
    @Query("""
        select b from Basket b
        join fetch b.subject s
        where s.id in :ids
        and s.isDeleted = false
        and s.semesterAt = :semesterAt
        """)
    List<Basket> findBySubjectIds(@Param("ids") List<Long> ids, @Param("semesterAt") String semesterAt);
}
