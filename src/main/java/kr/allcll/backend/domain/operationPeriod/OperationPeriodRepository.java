package kr.allcll.backend.domain.operationPeriod;

import java.util.List;
import java.util.Optional;
import kr.allcll.backend.support.semester.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OperationPeriodRepository extends JpaRepository<OperationPeriod, Long> {

    @Query("""
          SELECT p
          FROM OperationPeriod p
          WHERE p.semester = :semester
        """)
    List<OperationPeriod> findAllBySemester(@Param("semester") Semester semester);

    Optional<OperationPeriod> findBySemesterAndOperationType(Semester semester, OperationType operationType);

    void deleteBySemesterAndOperationType(Semester semester, OperationType operationType);
}
