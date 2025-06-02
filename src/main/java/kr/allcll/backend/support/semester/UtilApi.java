package kr.allcll.backend.support.semester;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UtilApi {

    private final UtilService utilService;

    @GetMapping("/api/service/semester")
    public ResponseEntity<SemesterResponse> getSemester() {
        SemesterResponse response = utilService.getSemester();
        return ResponseEntity.ok(response);
    }
}
