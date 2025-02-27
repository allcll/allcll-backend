package kr.allcll.backend.domain.seat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import kr.allcll.backend.domain.subject.Subject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "seats")
@Getter
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate createdDate;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private String smtCd;
    private Integer remainTotRcnt;
    private Integer totLimitRcnt1;
    private Integer outLimitRcnt;
    private String classCode;
    private Integer totLimitRcnt4;
    private Integer totLimitRcnt3;
    private Integer totLimitRcnt2;
    private String year;
    private Integer outLimitRcnt2;
    private Integer outLimitRcnt1;
    private Integer totRcnt;
    private String curiNo;
    private Integer outLimitRcnt4;
    private Integer outLimitRcnt3;
    private Integer totLimitRcnt;
    private Integer remainOtherRcnt;
    private Integer totOutRcnt;
    private Integer remainOutRcnt;
    private String deptCd;
    private String studentDivNm;
    private String orgnClsfCd;
    private String studentDiv;

    public Seat(LocalDate createdDate, Subject subject, String smtCd, Integer remainTotRcnt, Integer totLimitRcnt1,
        Integer outLimitRcnt,
        String classCode, Integer totLimitRcnt4, Integer totLimitRcnt3, Integer totLimitRcnt2, String year,
        Integer outLimitRcnt2, Integer outLimitRcnt1, Integer totRcnt, String curiNo, Integer outLimitRcnt4,
        Integer outLimitRcnt3, Integer totLimitRcnt, Integer remainOtherRcnt, Integer totOutRcnt, Integer remainOutRcnt,
        String deptCd, String studentDivNm, String orgnClsfCd, String studentDiv) {
        this.createdDate = createdDate;
        this.subject = subject;
        this.smtCd = smtCd;
        this.remainTotRcnt = remainTotRcnt;
        this.totLimitRcnt1 = totLimitRcnt1;
        this.outLimitRcnt = outLimitRcnt;
        this.classCode = classCode;
        this.totLimitRcnt4 = totLimitRcnt4;
        this.totLimitRcnt3 = totLimitRcnt3;
        this.totLimitRcnt2 = totLimitRcnt2;
        this.year = year;
        this.outLimitRcnt2 = outLimitRcnt2;
        this.outLimitRcnt1 = outLimitRcnt1;
        this.totRcnt = totRcnt;
        this.curiNo = curiNo;
        this.outLimitRcnt4 = outLimitRcnt4;
        this.outLimitRcnt3 = outLimitRcnt3;
        this.totLimitRcnt = totLimitRcnt;
        this.remainOtherRcnt = remainOtherRcnt;
        this.totOutRcnt = totOutRcnt;
        this.remainOutRcnt = remainOutRcnt;
        this.deptCd = deptCd;
        this.studentDivNm = studentDivNm;
        this.orgnClsfCd = orgnClsfCd;
        this.studentDiv = studentDiv;
    }

    public void merge(Seat seat) {
        id = seat.id;
        createdDate = seat.createdDate;
        subject = seat.subject;
    }
}
