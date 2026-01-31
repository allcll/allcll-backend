package kr.allcll.backend.domain.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.allcll.backend.support.entity.BaseEntity;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    private static final int YEAR_PREFIX = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentId;

    private int admissionYear;

    private String name;

    //private ProgramType programType; // 전공 형태

    private String deptNm;
    //private String deptCd;
    //private String collegeNm;

    //private String doubleCollegeNm;
    //private String doubleDeptCd;
    //private String doubleDeptNm;

    private User(String studentId, String name, String deptNm, int admissionYear) {
        this.studentId = studentId;
        this.name = name;
        this.deptNm = deptNm;
        this.admissionYear = admissionYear;
    }

    public static User of(String studentId, String name, String deptNm) {
        return new User(
            studentId,
            name,
            deptNm,
            extractAdmissionYear(studentId));
    }

    private static int extractAdmissionYear(String studentId) {
        if (studentId == null) {
            throw new AllcllException(AllcllErrorCode.STUDENT_ID_FETCH_FAIL, studentId);
        }
        int year = Integer.parseInt(studentId.substring(0, 2));
        return YEAR_PREFIX + year;
    }
}
