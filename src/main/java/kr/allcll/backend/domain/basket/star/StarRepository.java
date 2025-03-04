package kr.allcll.backend.domain.basket.star;

import java.util.List;
import kr.allcll.backend.domain.subject.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StarRepository extends JpaRepository<Star, Long> {

    @Query("select case when count(s) > 0 then true else false end "
        + "from Star s "
        + "join s.subject sub "
        + "where s.subject = :subject "
        + "and s.token = :token "
        + "and sub.deletedAt is null "
        + "and sub.isDeleted = false")
    boolean existsBySubjectAndToken(Subject subject, String token);

    @Query("select s from Star s "
        + "join fetch s.subject sub "
        + "where s.token = :token "
        + "and sub.deletedAt is null "
        + "and sub.isDeleted = false")
    List<Star> findAllByToken(String token);

    @Query("select count(s) from Star s "
        + "join s.subject sub "
        + "where s.token = :token "
        + "and sub.deletedAt is null "
        + "and sub.isDeleted = false")
    Long countAllByToken(String token);

    void deleteStarBySubjectIdAndToken(Long subjectId, String token);
}
