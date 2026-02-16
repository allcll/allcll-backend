package kr.allcll.backend.domain.graduation.credit;

import kr.allcll.backend.domain.graduation.credit.dto.GraduationCategoriesResponse;
import kr.allcll.backend.support.web.Auth;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GraduationCategoryApi {

    private final GraduationCategoryService graduationCategoryService;

    @GetMapping("/api/graduation/criteria/categories")
    public ResponseEntity<GraduationCategoriesResponse> getAllCategories(@Auth Long userId) {
        GraduationCategoriesResponse graduationCategoriesResponse = graduationCategoryService.getAllCategories(userId);
        return ResponseEntity.ok(graduationCategoriesResponse);
    }
}
