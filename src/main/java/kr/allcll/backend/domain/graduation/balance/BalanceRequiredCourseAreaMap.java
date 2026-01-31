package kr.allcll.backend.domain.graduation.balance;

import jakarta.persistence.*;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "balance_required_course_area_map")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BalanceRequiredCourseAreaMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear; // 입학년도

    @Column(name = "admission_year_short", nullable = false)
    private Integer admissionYearShort;

    @Column(name = "curi_no", nullable = false)
    private String curiNo; // 학수번호

    @Column(name = "curi_nm", nullable = false)
    private String curiNm; // 과목명

    @Enumerated(EnumType.STRING)
    @Column(name = "domain_type", nullable = false)
    private BalanceRequiredArea balanceRequiredArea; // 균형교양 영역

    public BalanceRequiredCourseAreaMap(
        Integer admissionYear,
        Integer admissionYearShort,
        String curiNo,
        String curiNm,
        BalanceRequiredArea balanceRequiredArea
    ) {
        this.admissionYear = admissionYear;
        this.admissionYearShort = admissionYearShort;
        this.curiNo = curiNo;
        this.curiNm = curiNm;
        this.balanceRequiredArea = balanceRequiredArea;
    }
}
