package kr.allcll.backend.domain.period;

import java.util.List;
import java.util.Optional;
import kr.allcll.backend.support.semester.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PeriodRepository extends JpaRepository<OperationPeriod, Long> {

    @Query("""
          SELECT p
          FROM OperationPeriod p
          WHERE p.semester = :semester
          ORDER BY p.operationType, p.startDate DESC, p.id DESC
        """)
    List<OperationPeriod> findAllBySemesterOrderByOperationTypeAndStartDateDesc(@Param("semester") Semester semester);

    Optional<OperationPeriod> findBySemesterAndOperationType(Semester semester, OperationType operationType);

    void deleteBySemesterAndOperationType(Semester semester, OperationType operationType);
}
