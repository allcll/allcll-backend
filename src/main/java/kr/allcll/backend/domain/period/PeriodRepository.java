package kr.allcll.backend.domain.period;

import java.util.List;
import java.util.Optional;
import kr.allcll.backend.support.semester.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PeriodRepository extends JpaRepository<Period, Long> {

    @Query("""
          SELECT p
          FROM Period p
          WHERE p.semester = :semester
          ORDER BY p.serviceType, p.startDate DESC, p.id DESC
        """)
    List<Period> findAllBySemesterOrderByServiceTypeAndStartDateDesc(@Param("semester") Semester semester);

    Optional<Period> findBySemesterAndServiceType(Semester semester, ServiceType serviceType);

    void deleteBySemesterAndServiceType(Semester semester, ServiceType serviceType);
}
