package kr.allcll.backend.basket;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.allcll.backend.BaseEntity;
import kr.allcll.backend.subject.Subject;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "baskets")
@Entity
@Getter
@NoArgsConstructor
public class Basket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "smt_cd")
    private String smtCd; // 학기 코드

    @Column(name = "`year`")
    private String year; // 연도

    @Column(name = "curi_no")
    private String curiNo; // 학수번호

    @Column(name = "class")
    private String classNumber; // 분반

    @Column(name = "student_div")
    private String studentDiv; // 학생 구분 코드

    @Column(name = "student_div_nm")
    private String studentDivNm; // 학생 구분명

    @Column(name = "student_dept_cd")
    private String studentDeptCd; // 학생 소속 학과 코드

    @Column(name = "student_dept_cd_nm")
    private String studentDeptCdNm; // 학생 소속 학과명

    @Column(name = "dept_cd")
    private String deptCd; // 개설 학과 코드

    @Column(name = "orgn_clsf_cd")
    private String orgnClsfCd; // 기관 분류 코드

    @Column(name = "tot_limit_rcnt")
    private Integer totLimitRcnt; // 총 정원

    @Column(name = "out_limit_rcnt")
    private Integer outLimitRcnt; // 외부 정원 제한

    @Column(name = "tot_out_rcnt")
    private Integer totOutRcnt; // 외부 수강 인원

    @Column(name = "remain_out_rcnt")
    private Integer remainOutRcnt; // 외부 수강 가능 잔여 인원

    @Column(name = "remain_other_rcnt")
    private Integer remainOtherRcnt; // 기타 남은 수강 가능 인원

    @Column(name = "rcnt")
    private Integer rcnt; // 현재 수강 신청 인원

    @Column(name = "tot_rcnt")
    private Integer totRcnt; // 총 수강 신청 인원

    public Basket(Subject subject, String smtCd, String year, String curiNo, String classNumber,
        String studentDiv, String studentDivNm, String studentDeptCd, String studentDeptCdNm, String deptCd,
        String orgnClsfCd, Integer totLimitRcnt, Integer outLimitRcnt, Integer totOutRcnt, Integer remainOutRcnt,
        Integer remainOtherRcnt, Integer rcnt, Integer totRcnt) {
        this.subject = subject;
        this.smtCd = smtCd;
        this.year = year;
        this.curiNo = curiNo;
        this.classNumber = classNumber;
        this.studentDiv = studentDiv;
        this.studentDivNm = studentDivNm;
        this.studentDeptCd = studentDeptCd;
        this.studentDeptCdNm = studentDeptCdNm;
        this.deptCd = deptCd;
        this.orgnClsfCd = orgnClsfCd;
        this.totLimitRcnt = totLimitRcnt;
        this.outLimitRcnt = outLimitRcnt;
        this.totOutRcnt = totOutRcnt;
        this.remainOutRcnt = remainOutRcnt;
        this.remainOtherRcnt = remainOtherRcnt;
        this.rcnt = rcnt;
        this.totRcnt = totRcnt;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public boolean isEmpty() {
        return this.rcnt == null || this.rcnt == 0;
    }
}
