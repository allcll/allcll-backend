package kr.allcll.backend.domain.graduation.certification;

import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;

public class MajorTargetTypeResolver {

    static EnglishTargetType resolveEnglishTargetType(GraduationDepartmentInfo primaryDept, GraduationDepartmentInfo doubleDept) {
        if (doubleDept == null) {
            return primaryDept.getEnglishTargetType();
        }
        EnglishTargetType primaryDeptEnglishTargetType = primaryDept.getEnglishTargetType();
        EnglishTargetType doubleDeptEnglishTargetType = doubleDept.getEnglishTargetType();

        if (primaryDeptEnglishTargetType == EnglishTargetType.ENGLISH_MAJOR || doubleDeptEnglishTargetType == EnglishTargetType.ENGLISH_MAJOR) {
            return EnglishTargetType.ENGLISH_MAJOR;
        }
        if (primaryDeptEnglishTargetType == EnglishTargetType.EXEMPT && doubleDeptEnglishTargetType == EnglishTargetType.EXEMPT) {
            return EnglishTargetType.EXEMPT;
        }
        return EnglishTargetType.NON_MAJOR;
    }

    static CodingTargetType resolveCodingTargetType(GraduationDepartmentInfo primaryDept, GraduationDepartmentInfo doubleDept) {
        if (doubleDept == null) {
            return primaryDept.getCodingTargetType();
        }
        CodingTargetType primaryDeptCodingTargetType = primaryDept.getCodingTargetType();
        CodingTargetType doubleDeptCodingTargetType = doubleDept.getCodingTargetType();

        if (primaryDeptCodingTargetType == CodingTargetType.CODING_MAJOR || doubleDeptCodingTargetType == CodingTargetType.CODING_MAJOR) {
            return CodingTargetType.CODING_MAJOR;
        }
        if (primaryDeptCodingTargetType == CodingTargetType.EXEMPT && doubleDeptCodingTargetType == CodingTargetType.EXEMPT) {
            return CodingTargetType.EXEMPT;
        }
        return CodingTargetType.NON_MAJOR;
    }
}
