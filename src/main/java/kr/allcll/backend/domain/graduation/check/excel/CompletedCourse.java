package kr.allcll.backend.domain.graduation.check.excel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "completed_courses")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompletedCourse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "curi_no")
    private String curiNo;

    @Column(name = "curi_nm")
    private String curiNm;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type")
    private CategoryType categoryType;

    @Column(name = "selected_area")
    private String selectedArea;

    @Column(name = "credits")
    private Double credits;

    @Column(name = "grade")
    private String grade;

    @Enumerated(EnumType.STRING)
    @Column(name = "major_scope")
    private MajorScope majorScope;

    public CompletedCourse(
        Long userId,
        String curiNo,
        String curiNm,
        CategoryType categoryType,
        String selectedArea,
        Double credits,
        String grade,
        MajorScope majorScope
    ) {
        this.userId = userId;
        this.curiNo = curiNo;
        this.curiNm = curiNm;
        this.categoryType = categoryType;
        this.selectedArea = selectedArea;
        this.credits = credits;
        this.grade = grade;
        this.majorScope = majorScope;
    }
}
