package kr.allcll.backend.domain.basket;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BasketRepository extends JpaRepository<Basket, Long> {

    @Query("select b from Basket b "
        + "join b.subject s "
        + "where s.id = :id "
        + "and s.isDeleted = false")
    List<Basket> findBySubjectId(Long id);
}
