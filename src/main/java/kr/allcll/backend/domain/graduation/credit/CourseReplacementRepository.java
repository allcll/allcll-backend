package kr.allcll.backend.domain.graduation.credit;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseReplacementRepository extends JpaRepository<CourseReplacement, Long> {

    @Query("""
            select c from CourseReplacement c
            where c.admissionYear = :admissionYear
            and c.legacyCuriNm in :legacyNames
            and c.enabled = true
        """)
    List<CourseReplacement> findByAdmissionYearAndLegacyCuriNmIn(
        Integer admissionYear,
        Collection<String> legacyNames
    );
}
