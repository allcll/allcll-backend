package kr.allcll.backend.domain.graduation.credit.dto;

import java.util.List;

public record GraduationCategoriesResponse(
    GraduationContextResponse context,
    List<GraduationCategoryResponse> categories
) {
    public static GraduationCategoriesResponse of(
        GraduationContextResponse context,
        List<GraduationCategoryResponse> categories
    ) {
        return new GraduationCategoriesResponse(context, categories);
    }
}
