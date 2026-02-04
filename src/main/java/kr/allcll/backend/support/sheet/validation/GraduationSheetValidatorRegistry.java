package kr.allcll.backend.support.sheet.validation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import org.springframework.stereotype.Component;

@Component
public class GraduationSheetValidatorRegistry {

    private final Map<String, GraduationSheetValidator> validators;

    public GraduationSheetValidatorRegistry(List<GraduationSheetValidator> validators) {
        this.validators = validators.stream()
            .collect(Collectors.toMap(GraduationSheetValidator::tabName, graduationSheetValidator -> graduationSheetValidator));
    }

    public GraduationSheetValidator get(String tabName) {
        GraduationSheetValidator graduationSheetValidator = validators.get(tabName);
        if (graduationSheetValidator == null) {
            throw new AllcllException(AllcllErrorCode.GOOGLE_SHEET_TAB_NOT_FOUND);
        }
        return graduationSheetValidator;
    }
}
