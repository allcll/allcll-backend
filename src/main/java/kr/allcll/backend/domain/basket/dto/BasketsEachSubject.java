package kr.allcll.backend.domain.basket.dto;

public record BasketsEachSubject(
    Long subjectId,
    Integer totalCount //총 인원
) {

    public static BasketsEachSubject of(Long subjectId, Integer totalCount) {
        return new BasketsEachSubject(subjectId, totalCount);
    }
}
