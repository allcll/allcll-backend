package kr.allcll.backend.domain.graduation.credit;

import jakarta.persistence.*;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "course_replacements")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseReplacement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear; // 입학년도

    @Column(name = "admission_year_short", nullable = false)
    private Integer admissionYearShort;

    @Column(name = "legacy_curi_nm", nullable = false)
    private String legacyCuriNm; // 과거 과목명

    @Column(name = "current_curi_no", nullable = false)
    private String currentCuriNo; // 현재 과목 코드

    @Column(name = "current_curi_nm", nullable = false)
    private String currentCuriNm; // 현재 과목명

    @Column(name = "enabled", nullable = false)
    private Boolean enabled; // 활성화 여부

    @Column(name = "note")
    private String note; // 비고

    public CourseReplacement(
        Integer admissionYear,
        Integer admissionYearShort,
        String legacyCuriNm,
        String currentCuriNo,
        String currentCuriNm,
        Boolean enabled,
        String note
    ) {
        this.admissionYear = admissionYear;
        this.admissionYearShort = admissionYearShort;
        this.legacyCuriNm = legacyCuriNm;
        this.currentCuriNo = currentCuriNo;
        this.currentCuriNm = currentCuriNm;
        this.enabled = enabled;
        this.note = note;
    }
}
