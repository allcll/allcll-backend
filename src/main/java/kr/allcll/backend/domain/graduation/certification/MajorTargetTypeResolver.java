package kr.allcll.backend.domain.graduation.certification;

import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;

public class MajorTargetTypeResolver {

    static EnglishTargetType resolveEnglishTargetType(GraduationDepartmentInfo primaryDept,
        GraduationDepartmentInfo doubleDept) {
        if (doubleDept == null) {
            return primaryDept.getEnglishTargetType();
        }
        EnglishTargetType primaryDeptEnglishTargetType = primaryDept.getEnglishTargetType();
        EnglishTargetType doubleDeptEnglishTargetType = doubleDept.getEnglishTargetType();

        if (EnglishTargetType.ENGLISH_MAJOR.equals(primaryDeptEnglishTargetType)
            || EnglishTargetType.ENGLISH_MAJOR.equals(doubleDeptEnglishTargetType)) {
            return EnglishTargetType.ENGLISH_MAJOR;
        }
        if (EnglishTargetType.EXEMPT.equals(primaryDeptEnglishTargetType)
            && EnglishTargetType.EXEMPT.equals(doubleDeptEnglishTargetType)) {
            return EnglishTargetType.EXEMPT;
        }
        return EnglishTargetType.NON_MAJOR;
    }

    static CodingTargetType resolveCodingTargetType(GraduationDepartmentInfo primaryDept,
        GraduationDepartmentInfo doubleDept) {
        if (doubleDept == null) {
            return primaryDept.getCodingTargetType();
        }
        CodingTargetType primaryDeptCodingTargetType = primaryDept.getCodingTargetType();
        CodingTargetType doubleDeptCodingTargetType = doubleDept.getCodingTargetType();

        if (CodingTargetType.CODING_MAJOR.equals(primaryDeptCodingTargetType)
            || CodingTargetType.CODING_MAJOR.equals(doubleDeptCodingTargetType)) {
            return CodingTargetType.CODING_MAJOR;
        }
        if (CodingTargetType.EXEMPT.equals(primaryDeptCodingTargetType)
            && CodingTargetType.EXEMPT.equals(doubleDeptCodingTargetType)) {
            return CodingTargetType.EXEMPT;
        }
        return CodingTargetType.NON_MAJOR;
    }
}
