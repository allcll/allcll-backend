package kr.allcll.backend.domain.graduation.balance;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BalanceRequiredCourseAreaMapRepository extends JpaRepository<BalanceRequiredCourseAreaMap, Long> {

    @Query("""
            select m from BalanceRequiredCourseAreaMap m
            where m.admissionYear = :admissionYear
        """)
    List<BalanceRequiredCourseAreaMap> findAllByAdmissionYear(Integer admissionYear);
}
