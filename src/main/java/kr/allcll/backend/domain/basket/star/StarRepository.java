package kr.allcll.backend.domain.basket.star;

import java.util.List;
import kr.allcll.backend.domain.subject.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StarRepository extends JpaRepository<Star, Long> {

    boolean existsBySubjectAndToken(Subject subject, String token);

    List<Star> findAllByToken(String token);

    Long countAllByToken(String token);

    void deleteStarBySubjectIdAndToken(Long subjectId, String token);
}
