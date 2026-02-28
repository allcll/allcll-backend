package kr.allcll.backend.domain.graduation.check.result;

import java.util.List;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCoursePersistenceService;
import kr.allcll.backend.domain.graduation.check.excel.GradeExcelParser;
import kr.allcll.backend.domain.graduation.check.result.dto.CheckResult;
import kr.allcll.backend.domain.graduation.check.result.dto.CompletedCoursesResponse;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCheckResponse;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class GraduationCheckService {

    private final GradeExcelParser gradeExcelParser;
    private final GraduationChecker graduationChecker;
    private final GraduationCheckRepository graduationCheckRepository;
    private final GraduationCheckResponseMapper graduationCheckResponseMapper;
    private final CompletedCoursePersistenceService completedCoursePersistenceService;
    private final GraduationCheckPersistenceService graduationCheckPersistenceService;

    public void checkGraduationRequirements(Long userId, MultipartFile gradeExcel) {
        validateExcelFile(gradeExcel);

        // 1. 엑셀 파싱
        List<CompletedCourseDto> parsedCourses = gradeExcelParser.parse(gradeExcel);
        // 2. 이수과목 DB 저장
        List<CompletedCourse> savedCourses
            = completedCoursePersistenceService.saveAllCompletedCourse(userId, parsedCourses);
        // 3. 졸업 요건 검사 수행
        CheckResult checkResult = graduationChecker.calculate(userId, savedCourses);
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

    public CompletedCoursesResponse getAllEarnedCourses(Long userId) {
        List<CompletedCourse> completedCourses = completedCoursePersistenceService.getCompletedCourses(userId);
        return CompletedCoursesResponse.from(completedCourses);
    }
}
