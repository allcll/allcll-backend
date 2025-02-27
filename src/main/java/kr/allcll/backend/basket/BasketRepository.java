package kr.allcll.backend.basket;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BasketRepository extends JpaRepository<Basket, Long> {

    List<Basket> findBySubjectId(Long id);
}
