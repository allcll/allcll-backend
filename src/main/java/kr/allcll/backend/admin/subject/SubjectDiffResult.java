package kr.allcll.backend.admin.subject;

public record SubjectDiffResult(
    Long id,
    String curiNo,
    String curiNm,
    String lesnRoom,
    String lesnTime,
    String lesnEmp,
    String isDeleted
) {

}
