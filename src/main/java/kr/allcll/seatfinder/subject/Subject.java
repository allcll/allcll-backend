package kr.allcll.seatfinder.subject;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.allcll.seatfinder.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "subjects")
@Entity
@Getter
@EqualsAndHashCode(of = "id", callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class Subject extends BaseEntity {

    private static final String NON_MAJOR_DEPARTMENT_NAME = "대양휴머니티칼리지";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "everytime_lecture_id")
    private Long everytimeLectureId; // 에브리타임 강의 ID

    @Column(name = "smt_cd")
    private String smtCd; // 학기 코드

    @Column(name = "smt_cd_nm")
    private String smtCdNm; // 학기명

    @Column(name = "`year`")
    private String year; // 연도

    @Column(name = "year_smt_nm")
    private String yearSmtNm; // 연도 및 학기명

    @Column(name = "curi_no")
    private String curiNo; // 학수번호

    @Column(name = "curi_nm")
    private String curiNm; // 과목명

    @Column(name = "curi_type_cd")
    private String curiTypeCd; // 과목 유형 코드

    @Column(name = "curi_type_cd_nm")
    private String curiTypeCdNm; // 이수 구분

    @Column(name = "cdt")
    private String cdt; // 학점

    @Column(name = "tm_num")
    private String tmNum; // 수업 시간(학점/이론/실습)

    @Column(name = "tot_tm_num")
    private String totTmNum; // 총 수업 시간

    @Column(name = "student_year")
    private String studentYear; // 수강 가능 학년

    @Column(name = "lesn_time")
    private String lesnTime; // 강의 시간

    @Column(name = "lesn_room")
    private String lesnRoom; // 강의실

    @Column(name = "lesn_emp")
    private String lesnEmp; // 담당 교수

    @Column(name = "cors_unit_grp_cd")
    private String corsUnitGrpCd; // 과정 코드

    @Column(name = "cors_unit_grp_cd_nm")
    private String corsUnitGrpCdNm; // 과정명

    @Column(name = "orgn_clsf_cd")
    private String orgnClsfCd; // 기관 분류 코드

    @Column(name = "orgn_clsf_cd_nm")
    private String orgnClsfCdNm; // 기관 분류명

    @Column(name = "dept_cd")
    private String deptCd; // 학과 코드

    @Column(name = "sch_dept_alias")
    private String schDeptAlias; // 학과명 (약칭)

    @Column(name = "manage_dept_nm")
    private String manageDeptNm; // 개설 학과

    @Column(name = "class")
    private String className; // 분반

    @Column(name = "curi_lang_cd")
    private String curiLangCd; // 강의 언어 코드

    @Column(name = "curi_lang_nm")
    private String curiLangNm; // 강의 언어

    @Column(name = "internship_type_cd_nm")
    private String internshipTypeCdNm; // 인턴십 유형

    @Column(name = "cyber_type_cd_nm")
    private String cyberTypeCdNm; // 사이버 강의 여부

    @Column(name = "inout_sub_cdt_exchange_yn")
    private String inoutSubCdtExchangeYn; // 교환학생 여부

    @Column(name = "plan_reg_yn")
    private String planRegYn; // 수업 계획서 등록 여부

    @Column(name = "slt_domain_cd")
    private String sltDomainCd; // 선택 영역 코드

    @Column(name = "slt_domain_cd_nm")
    private String sltDomainCdNm; // 선택 영역명

    @Column(name = "remark")
    private String remark; // 비고

    @Column(name = "prt_ord")
    private String prtOrd; // 정렬 순서

    public Subject(String smtCd, String smtCdNm, String year, String yearSmtNm, String curiNo, String curiNm,
        String curiTypeCd, String curiTypeCdNm, String cdt, String tmNum, String totTmNum, String studentYear,
        String lesnTime, String lesnRoom, String lesnEmp, String corsUnitGrpCd, String corsUnitGrpCdNm,
        String orgnClsfCd,
        String orgnClsfCdNm, String deptCd, String schDeptAlias, String manageDeptNm, String className,
        String curiLangCd,
        String curiLangNm, String internshipTypeCdNm, String cyberTypeCdNm, String inoutSubCdtExchangeYn,
        String planRegYn,
        String sltDomainCd, String sltDomainCdNm, String remark, String prtOrd) {
        this.smtCd = smtCd;
        this.smtCdNm = smtCdNm;
        this.year = year;
        this.yearSmtNm = yearSmtNm;
        this.curiNo = curiNo;
        this.curiNm = curiNm;
        this.curiTypeCd = curiTypeCd;
        this.curiTypeCdNm = curiTypeCdNm;
        this.cdt = cdt;
        this.tmNum = tmNum;
        this.totTmNum = totTmNum;
        this.studentYear = studentYear;
        this.lesnTime = lesnTime;
        this.lesnRoom = lesnRoom;
        this.lesnEmp = lesnEmp;
        this.corsUnitGrpCd = corsUnitGrpCd;
        this.corsUnitGrpCdNm = corsUnitGrpCdNm;
        this.orgnClsfCd = orgnClsfCd;
        this.orgnClsfCdNm = orgnClsfCdNm;
        this.deptCd = deptCd;
        this.schDeptAlias = schDeptAlias;
        this.manageDeptNm = manageDeptNm;
        this.className = className;
        this.curiLangCd = curiLangCd;
        this.curiLangNm = curiLangNm;
        this.internshipTypeCdNm = internshipTypeCdNm;
        this.cyberTypeCdNm = cyberTypeCdNm;
        this.inoutSubCdtExchangeYn = inoutSubCdtExchangeYn;
        this.planRegYn = planRegYn;
        this.sltDomainCd = sltDomainCd;
        this.sltDomainCdNm = sltDomainCdNm;
        this.remark = remark;
        this.prtOrd = prtOrd;
    }

    public boolean isNonMajor() {
        return NON_MAJOR_DEPARTMENT_NAME.equals(manageDeptNm);
    }
}
