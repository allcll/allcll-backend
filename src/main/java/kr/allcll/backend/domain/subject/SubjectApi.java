package kr.allcll.backend.domain.subject;

import kr.allcll.backend.domain.subject.dto.SubjectsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SubjectApi {

    private final SubjectService subjectService;

    @GetMapping("/api/subjects")
    public ResponseEntity<SubjectsResponse> getSubjects(
        @RequestParam(required = false) Long subjectId,
        @RequestParam(required = false) String subjectName,
        @RequestParam(required = false) String subjectCode,
        @RequestParam(required = false) String classCode,
        @RequestParam(required = false) String professorName
    ) {
        SubjectsResponse subjectsResponse = subjectService.findSubjectsByCondition(
            subjectId,
            subjectName,
            subjectCode,
            classCode,
            professorName
        );
        return ResponseEntity.ok(subjectsResponse);
    }
}
