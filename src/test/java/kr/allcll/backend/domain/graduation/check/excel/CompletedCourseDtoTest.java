package kr.allcll.backend.domain.graduation.check.excel;

import static org.assertj.core.api.Assertions.assertThat;

import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CompletedCourseDtoTest {

    @Test
    @DisplayName("복수전공 이수구분(복필/복선/복기)은 SECONDARY 전공 범위로 판별한다.")
    void determineMajorScope_secondaryMajorCategories() {
        // when && then
        assertThat(CompletedCourseDto.of("1", "과목", "복필", "", 3.0, "A+").majorScope())
            .isEqualTo(MajorScope.SECONDARY);
        assertThat(CompletedCourseDto.of("2", "과목", "복선", "", 3.0, "A+").majorScope())
            .isEqualTo(MajorScope.SECONDARY);
        assertThat(CompletedCourseDto.of("3", "과목", "복기", "", 3.0, "A+").majorScope())
            .isEqualTo(MajorScope.SECONDARY);
    }

    @Test
    @DisplayName("주전공 이수구분(전필/전기)은 PRIMARY 전공 범위로 판별한다.")
    void determineMajorScope_primaryMajorCategories() {
        // when && then
        assertThat(CompletedCourseDto.of("1", "과목", "전필", "", 3.0, "A+").majorScope())
            .isEqualTo(MajorScope.PRIMARY);
        assertThat(CompletedCourseDto.of("2", "과목", "전기", "", 3.0, "A+").majorScope())
            .isEqualTo(MajorScope.PRIMARY);
    }

    @Test
    @DisplayName("이수구분 '복기' 과목도 엔티티 변환에 성공한다.")
    void toEntity_doubleMajorBasicCourse() {
        // given: 신고 성적표 12행 — 2024-2학기 선형대수(001725) 복기
        CompletedCourseDto dto = CompletedCourseDto.of(
            "001725", "선형대수", "복기", "", 3.0, "B+");

        // when
        CompletedCourse entity = dto.toEntity(1L, 2026);

        // then
        assertThat(entity.getCategoryType()).isEqualTo(CategoryType.MAJOR_BASIC);
        assertThat(entity.getMajorScope()).isEqualTo(MajorScope.SECONDARY);
    }

    @Test
    @DisplayName("23학번 이하의 '복기' 과목은 학문기초(ACADEMIC_BASIC)로 보정되어 엔티티 변환에 성공한다.")
    void toEntity_doubleMajorBasicCourse_whenAdmissionYearBefore2024() {
        // given: 신고 성적표 12행 — 2020학번 복수전공자의 2024-2학기 선형대수(001725) 복기
        CompletedCourseDto dto = CompletedCourseDto.of(
            "001725", "선형대수", "복기", "", 3.0, "B+");

        // when
        CompletedCourse entity = dto.toEntity(1L, 2020);

        // then
        assertThat(entity.getCategoryType()).isEqualTo(CategoryType.ACADEMIC_BASIC);
        assertThat(entity.getMajorScope()).isEqualTo(MajorScope.SECONDARY);
    }

    @Test
    @DisplayName("이수구분 'ROTC' 과목은 엔티티 변환에 성공한다.")
    void toEntity_rotcCourse() {
        // given: B8 신고(23010678) 성적표 5행 — 2026-1학기 조직리더십(010655) ROTC
        CompletedCourseDto dto = CompletedCourseDto.of(
            "010655", "조직리더십", "ROTC", "", 3.0, "B+");

        // when
        CompletedCourse entity = dto.toEntity(1L, 2023);

        // then
        assertThat(entity.getCategoryType()).isEqualTo(CategoryType.ROTC);
        assertThat(entity.getMajorScope()).isEqualTo(MajorScope.PRIMARY);
        assertThat(entity.isEarned()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 이수구분 과목은 ETC 로 fallback 되어 엔티티 변환에 성공한다.")
    void toEntity_unknownCategoryFallsBackToEtc() {
        // given: alias 미등록 이수구분 (예: 교직) — 업로드 전체 실패 대신 ETC 수용
        CompletedCourseDto dto = CompletedCourseDto.of(
            "999999", "교육학개론", "교직", "", 2.0, "A0");

        // when
        CompletedCourse entity = dto.toEntity(1L, 2023);

        // then
        assertThat(entity.getCategoryType()).isEqualTo(CategoryType.ETC);
        assertThat(entity.getMajorScope()).isEqualTo(MajorScope.PRIMARY);
        assertThat(entity.isEarned()).isTrue();
    }
}
