package kr.allcll.seatfinder.support.fixture;

import kr.allcll.seatfinder.subject.Subject;

public class SubjectFixture {

    public static Subject createSubject(
        String subjectName,
        String subjectCode,
        String classCode,
        String professorName
    ) {
        return new Subject("", "", "", "",
            subjectCode, subjectName,
            "", "", "", "", "", "", "", "",
            professorName, "", "", "", "", "",
            "", "",
            classCode, "", "", "", "", "",
            "", "", "", "", "");
    }

    public static Subject createSubjectWithDepartmentCode(
        String subjectName,
        String subjectCode,
        String classCode,
        String professorName,
        String departmentCode
    ) {
        return new Subject("", "", "", "",
            subjectCode, subjectName,
            "", "", "", "", "", "", "", "",
            professorName, "", "", "", "", departmentCode,
            "", "",
            classCode, "", "", "", "", "",
            "", "", "", "", "");
    }

    public static Subject createSubjectWithDepartmentInformation(
        String subjectName, //과목명
        String departmentName, //개설학과
        String departmentCode, //개설 학과코드
        String subjectCode, // 학수번호
        String classCode, //분반
        String professorName
    ) {
        return new Subject("", "", "", "",
            subjectCode, subjectName,
            "", "", "", "", "", "", "", "",
            professorName, "", "", "", "", departmentCode,
            "", departmentName,
            classCode, "", "", "", "", "",
            "", "", "", "", "");
    }

    public static Subject createNonMajorSubject(
        Long subjectId,
        String subjectName,
        String subjectCode,
        String classCode,
        String professorName
    ) {
        return new Subject(subjectId, 0L,
            "", "", "", "",
            subjectCode, subjectName, "", "", "", "", "", "",
            "", "",
            professorName, "", "", "", "", "3210",
            "",
            "대양휴머니티칼리지", classCode, "", "", "",
            "", "", "", "", "",
            "", "");
    }

    public static Subject createMajorSubject(
        Long subjectId,
        String subjectName,
        String subjectCode,
        String classCode,
        String professorName
    ) {
        return new Subject(subjectId, 0L,
            "", "", "", "",
            subjectCode, subjectName, "", "", "", "", "", "",
            "", "",
            professorName, "", "", "", "", "3210",
            "",
            "컴퓨터공학과", classCode, "", "", "",
            "", "", "", "", "",
            "", "");
    }
}
