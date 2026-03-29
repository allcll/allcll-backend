package kr.allcll.backend.domain.notice;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("""
        select n from Notice n
        where n.isDeleted = false
        order by n.createdAt desc
        """)
    List<Notice> findAllOrderedByCreatedAt();

    @Query("""
        select n from Notice n
        where n.id = :id
        and n.isDeleted = false
        """)
    Optional<Notice> findActiveById(Long id);
}
