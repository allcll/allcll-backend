package kr.allcll.backend.domain.basket.star;

import java.util.List;
import kr.allcll.backend.domain.subject.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface StarRepository extends JpaRepository<Star, Long> {

    @Query("select case when count(s) > 0 then true else false end "
        + "from Star s "
        + "join s.subject sub "
        + "where s.subject = :subject "
        + "and s.token = :token "
        + "and sub.isDeleted = false "
        + "and sub.semesterAt = :semesterAt")
    boolean existsBySubjectAndToken(Subject subject, String token, String semesterAt);

    @Query("select s from Star s "
        + "join fetch s.subject sub "
        + "where s.token = :token "
        + "and sub.isDeleted = false "
        + "and sub.semesterAt = :semesterAt")
    List<Star> findAllByToken(String token, String semesterAt);

    @Query("select count(s) from Star s "
        + "join s.subject sub "
        + "where s.token = :token "
        + "and sub.isDeleted = false "
        + "and sub.semesterAt = :semesterAt")
    Long countAllByToken(String token, String semesterAt);

    @Modifying
    @Query("delete from Star s "
        + "where s.subject.id = :subjectId "
        + "and s.token = :token "
        + "and s.subject.semesterAt = :semesterAt")
    void deleteStarBySubjectIdAndToken(Long subjectId, String token, String semesterAt);
}
