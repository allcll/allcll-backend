package kr.allcll.backend.star;

import kr.allcll.backend.ThreadLocalHolder;
import kr.allcll.backend.star.dto.StarredSubjectIdsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StarApi {

    private final StarService starService;

    @PostMapping("/api/stars")
    ResponseEntity<Void> addStarOnSubject(@RequestParam Long subjectId) {
        starService.addStarOnSubject(subjectId, ThreadLocalHolder.SHARED_TOKEN.get());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/stars/{subjectId}")
    public ResponseEntity<Void> deleteStarOnSubject(@PathVariable Long subjectId) {
        starService.deleteStarOnSubject(subjectId, ThreadLocalHolder.SHARED_TOKEN.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/stars")
    public ResponseEntity<StarredSubjectIdsResponse> retrieveStars() {
        StarredSubjectIdsResponse response = starService.retrieveStars(ThreadLocalHolder.SHARED_TOKEN.get());
        return ResponseEntity.ok(response);
    }
}
