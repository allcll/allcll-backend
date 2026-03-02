package kr.allcll.backend.domain.review;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReviewRepository extends JpaRepository<UserReview, Long> {

    @Query("""
            select ur from UserReview ur
            where ur.studentId = :studentId
        """)
    List<UserReview> findReviews(String studentId);

}
