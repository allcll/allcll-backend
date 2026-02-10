package kr.allcll.backend.domain.subject;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface SubjectRepository extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {

    @Query("""
        select s from Subject s
        where s.deptCd = :deptCd
        and s.isDeleted = false
        and s.semesterAt = :semesterAt
        """)
    List<Subject> findAllByDeptCd(String deptCd, String semesterAt);

    @Query("select s from Subject s where s.isDeleted = false")
    List<Subject> findAll();

    @Query("""
        select s from Subject s
        where s.id = :id
        and s.isDeleted = false
        and s.semesterAt = :semesterAt
        """)
    Optional<Subject> findById(Long id, String semesterAt);

    @Query("""
        select s
        from Subject s
        where s.deptCd = :deptCd
          and s.curiTypeCdNm = :curiTypeCdNm
        order by s.curiNo asc
    """)
    List<Subject> findByDeptCdAndCuriTypeCdNm(
        String deptCd,
        String curiTypeCdNm
    );
}
