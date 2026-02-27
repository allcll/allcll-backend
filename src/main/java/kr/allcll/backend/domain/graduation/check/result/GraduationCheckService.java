package kr.allcll.backend.domain.graduation.check.result;

import java.util.List;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCoursePersistenceService;
import kr.allcll.backend.domain.graduation.check.excel.GradeExcelParser;
import kr.allcll.backend.domain.graduation.check.result.dto.CheckResult;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCheckResponse;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GraduationCheckService {

    private final GradeExcelParser gradeExcelParser;
    private final GraduationChecker graduationChecker;
    private final GraduationCheckRepository graduationCheckRepository;
    private final GraduationCheckResponseMapper graduationCheckResponseMapper;
    private final CompletedCoursePersistenceService completedCoursePersistenceService;
    private final GraduationCheckPersistenceService graduationCheckPersistenceService;

    @Transactional
    public void checkGraduationRequirements(Long userId, MultipartFile gradeExcel) {
        validateExcelFile(gradeExcel);

        // 1. 엑셀 파싱
        List<CompletedCourseDto> completedCourseDtos = gradeExcelParser.parse(gradeExcel);
        // 2. 이수과목 DB 저장
        completedCoursePersistenceService.saveAllCompletedCourse(userId, completedCourseDtos);
        // 3. 졸업 요건 검사 수행
        CheckResult checkResult = graduationChecker.calculate(userId);
        // 4. 검사 결과 저장
        graduationCheckPersistenceService.saveCheckResult(userId, checkResult);
    }

    public GraduationCheckResponse getCheckResult(Long userId) {
        GraduationCheck check = graduationCheckRepository
            .findById(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.GRADUATION_CHECK_NOT_FOUND));
        return graduationCheckResponseMapper.toResponseFromEntity(check);
    }

    private void validateExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AllcllException(AllcllErrorCode.INVALID_INPUT_VALUE);
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx")) {
            throw new AllcllException(AllcllErrorCode.INVALID_FILE_TYPE);
        }
    }
}
