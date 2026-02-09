package kr.allcll.backend.support.sheet.validation;

import kr.allcll.backend.support.sheet.GraduationSheetTable;

public interface GraduationSheetValidator {

    String tabKey();

    void validate(GraduationSheetTable sheetTable);
}
