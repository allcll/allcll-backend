package kr.allcll.backend.domain.graduation.check.excel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradeExcelParser {

    private static final int SHEET_START_ROW = 3;

    public List<CompletedCourseDto> parse(MultipartFile file) {
        List<CompletedCourseDto> completedCourses = new ArrayList<>();

        InputStream stream = null;
        Workbook workbook = null;
        try {
            stream = file.getInputStream();
            workbook = new XSSFWorkbook(stream);

            Sheet sheet = workbook.getSheetAt(0);
            parseSheet(sheet, completedCourses);

            return completedCourses;
        } catch (IOException exception) {
            throw new AllcllException(AllcllErrorCode.EXCEL_PARSE_ERROR, exception);
        }
    }

    private void parseSheet(Sheet sheet, List<CompletedCourseDto> completedCourses) {
        for (int rowNumber = SHEET_START_ROW; rowNumber <= sheet.getLastRowNum(); rowNumber++) {
            Row row = sheet.getRow(rowNumber);
            if (row == null) {
                continue;
            }

            try {
                CompletedCourseDto course = parseRow(row);
                if (course.isCreditEarned()) {
                    completedCourses.add(course);
                }
            } catch (Exception exception) {
                log.warn("엑셀 파싱 실패 - row {}: {}", rowNumber, exception.getMessage());
            }
        }
    }

    private CompletedCourseDto parseRow(Row row) {
        // 컬럼: 순번(0), 년도(1), 학기(2), 학수번호(3), 교과목명(4), 이수구분(5), 교직영역(6), 선택영역(7), 학점(8), 평가방식(9), 등급(10), 평점(11), 개설학과코드(12)
        String curiNo = getCellValue(row.getCell(3));
        String curiNm = getCellValue(row.getCell(4));
        String categoryTypeRaw = getCellValue(row.getCell(5));
        String selectedArea = getCellValue(row.getCell(7));
        String creditsStr = getCellValue(row.getCell(8));
        String grade = getCellValue(row.getCell(10));

        //필수 칼럼 검증
        validateRequiredColumn(curiNo);
        validateRequiredColumn(curiNm);
        validateRequiredColumn(categoryTypeRaw);
        validateRequiredColumn(creditsStr);

        Double credits = parseCredits(creditsStr);

        return CompletedCourseDto.of(curiNo, curiNm, categoryTypeRaw, selectedArea, credits, grade);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        return cell.toString().trim();
    }

    private void validateRequiredColumn(String column) {
        if (column == null || column.trim().isEmpty()) {
            throw new AllcllException(AllcllErrorCode.EMPTY_REQUIRED_COLUMN);
        }
    }

    private Double parseCredits(String creditsStr) {
        try {
            return Double.parseDouble(creditsStr.trim());
        } catch (NumberFormatException e) {
            throw new AllcllException(AllcllErrorCode.INVALID_CREDIT_COLUMN);
        }
    }
}
