package kr.allcll.backend.support.sheet;

public interface GraduationSheetValidator {

    String tabName();

    void validate(GraduationSheetTable sheetTable);
}
