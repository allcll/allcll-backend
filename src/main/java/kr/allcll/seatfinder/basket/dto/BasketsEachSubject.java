package kr.allcll.seatfinder.basket.dto;

import java.util.List;
import kr.allcll.seatfinder.basket.Basket;
import kr.allcll.seatfinder.subject.Subject;

public record BasketsEachSubject(
    Long subjectId,
    String subjectName, //과목명
    String departmentName, //개설학과
    String departmentCode, //개설 학과코드
    String subjectCode, // 학수번호
    String classCode, //분반
    String professorName, //교수명
    Integer totalCount //총 인원
) {

    public static BasketsEachSubject from(Subject subject, List<Basket> baskets) {
        if (baskets.isEmpty()) {
            return new BasketsEachSubject(
                subject.getId(),
                subject.getCuriNm(),
                subject.getManageDeptNm(),
                subject.getDeptCd(),
                subject.getCuriNo(),
                subject.getClassName(),
                subject.getLesnEmp(),
                0
            );
        }
        return new BasketsEachSubject(
            subject.getId(),
            subject.getCuriNm(),
            subject.getManageDeptNm(),
            subject.getDeptCd(),
            subject.getCuriNo(),
            subject.getClassName(),
            subject.getLesnEmp(),
            baskets.getFirst().getTotRcnt()
        );
    }
}
