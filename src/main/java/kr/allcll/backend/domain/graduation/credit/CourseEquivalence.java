package kr.allcll.backend.domain.graduation.credit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "course_equivalences")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseEquivalence extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_code", nullable = false)
    private String groupCode; // 동일과목 그룹 번호

    @Column(name = "curi_no", nullable = false)
    private String curiNo; // 학수번호

    @Column(name = "curi_nm", nullable = false, length = 255)
    private String curiNm; // 과목명

    public CourseEquivalence(String groupCode, String curiNo, String curiNm) {
        this.groupCode = groupCode;
        this.curiNo = curiNo;
        this.curiNm = curiNm;
    }
}
