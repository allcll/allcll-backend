package kr.allcll.backend.domain.user;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentId;

    private String name;

    private int admissionYear;

    @Enumerated(EnumType.STRING)
    private MajorType majorType;

    private String collegeNm;
    private String deptNm;
    private String deptCd;

    private String doubleCollegeNm;
    private String doubleDeptNm;
    private String doubleDeptCd;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private GraduationCheckCertResult graduationCheckCertResult;

    public User(String studentId, String name, int admissionYear, MajorType majorType, String collegeNm, String deptNm,
        String deptCd, String doubleCollegeNm, String doubleDeptNm, String doubleDeptCd) {
        this.studentId = studentId;
        this.name = name;
        this.admissionYear = admissionYear;
        this.majorType = majorType;
        this.collegeNm = collegeNm;
        this.deptNm = deptNm;
        this.deptCd = deptCd;
        this.doubleCollegeNm = doubleCollegeNm;
        this.doubleDeptNm = doubleDeptNm;
        this.doubleDeptCd = doubleDeptCd;
    }

    public void updateUser(String deptNm) {
        this.deptNm = deptNm;
    }
}
