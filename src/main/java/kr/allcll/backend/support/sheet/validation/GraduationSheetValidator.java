package kr.allcll.backend.support.sheet.validation;

import kr.allcll.backend.support.sheet.GraduationSheetTable;

public interface GraduationSheetValidator {

    String tabName();

    void validate(GraduationSheetTable sheetTable);
}
