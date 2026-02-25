package kr.allcll.backend.domain.graduation.check.excel;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CompletedCourseRepository extends JpaRepository<CompletedCourse, Long> {

    @Query(""" 
        select c from CompletedCourse c
        where c.userId = :userId
        """)
    List<CompletedCourse> findAllByUserId(Long userId);


    @Query("""
        delete from CompletedCourse c
        where c.userId = :userId
        """)
    void deleteByUserId(Long userId);
}
