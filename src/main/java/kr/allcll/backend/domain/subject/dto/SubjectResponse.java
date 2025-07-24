package kr.allcll.backend.domain.subject.dto;

import kr.allcll.backend.domain.subject.Subject;

public record SubjectResponse(
    Long subjectId,
    String subjectName,
    String subjectCode,
    String classCode,
    String professorName,
    String deptCd,
    String manageDeptNm,
    String studentYear,
    String lesnTime,
    String lesnRoom,
    String tmNum
) {

    public static SubjectResponse from(Subject subject) {
        return new SubjectResponse(
            subject.getId(),
            subject.getCuriNm(),
            subject.getCuriNo(),
            subject.getClassName(),
            subject.getLesnEmp(),
            subject.getDeptCd(),
            subject.getManageDeptNm(),
            subject.getStudentYear(),
            subject.getLesnTime(),
            subject.getLesnRoom(),
            subject.getTmNum()
        );
    }
}
