package kr.allcll.backend.domain.graduation.check.cert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import kr.allcll.backend.domain.graduation.certification.ClassicAltCoursePolicy;
import kr.allcll.backend.domain.graduation.check.cert.dto.ClassicsCounts;
import kr.allcll.backend.domain.graduation.check.cert.dto.ClassicsResult;
import kr.allcll.backend.domain.user.User;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraduationClassicsCertResolverTest {

    @Mock
    private GraduationClassicsCertFetcher graduationClassicsCertFetcher;

    @Mock
    private ClassicAltCoursePolicy classicAltCoursePolicy;

    @Mock
    private User user;

    @Mock
    private OkHttpClient client;

    @InjectMocks
    private GraduationClassicsCertResolver resolver;

    @Test
    @DisplayName("DB에 고전인증 통과 이력이 있으면 fallback counts 로 통과 처리한다")
    void resolve_whenAlreadyPassed() {
        // given
        GraduationCheckCertResult certResult = mock(GraduationCheckCertResult.class);
        given(certResult.getIsClassicsCertPassed()).willReturn(true);
        given(certResult.getMyCountWestern()).willReturn(4);
        given(certResult.getMyCountEastern()).willReturn(2);
        given(certResult.getMyCountEasternAndWestern()).willReturn(2);
        given(certResult.getMyCountScience()).willReturn(1);

        // when
        ClassicsResult result = resolver.resolve(user, client, certResult);

        // then
        assertThat(result.passed()).isTrue();
    }

    @Test
    @DisplayName("크롤링 결과 인증 완료이면 통과 처리한다")
    void resolve_whenPassedByCrawledPass() {
        // given
        given(graduationClassicsCertFetcher.fetchClassics(client))
            .willReturn(new ClassicsResult(true, new ClassicsCounts(1, 0, 0, 0)));
        given(classicAltCoursePolicy.isSatisfiedByAltCourse(user)).willReturn(false);

        // when
        ClassicsResult result = resolver.resolve(user, client, null);

        // then
        assertThat(result.passed()).isTrue();
    }

    @Test
    @DisplayName("크롤링 결과의 인증 실패이지만, 모든 영역의 필요 이수 권수를 만족하면 통과 처리한다")
    void resolve_whenPassedByCounts() {
        // given
        given(graduationClassicsCertFetcher.fetchClassics(client))
            .willReturn(new ClassicsResult(false, new ClassicsCounts(4, 2, 3, 1)));
        given(classicAltCoursePolicy.isSatisfiedByAltCourse(user)).willReturn(false);

        // when
        ClassicsResult result = resolver.resolve(user, client, null);

        // then
        assertThat(result.passed()).isTrue();
    }

    @Test
    @DisplayName("대체과목을 만족하면 통과 처리한다")
    void resolve_whenPassedByAltCourse() {
        // given
        given(graduationClassicsCertFetcher.fetchClassics(client))
            .willReturn(new ClassicsResult(false, new ClassicsCounts(1, 0, 0, 0)));
        given(classicAltCoursePolicy.isSatisfiedByAltCourse(user)).willReturn(true);

        // when
        ClassicsResult result = resolver.resolve(user, client, null);

        // then
        assertThat(result.passed()).isTrue();
    }

    @Test
    @DisplayName("크롤링 결과도 불합격이고 대체과목도 미충족이면 실패 처리한다")
    void resolve_whenFailed() {
        // given
        given(graduationClassicsCertFetcher.fetchClassics(client))
            .willReturn(new ClassicsResult(false, new ClassicsCounts(1, 0, 0, 0)));
        given(classicAltCoursePolicy.isSatisfiedByAltCourse(user)).willReturn(false);

        // when
        ClassicsResult result = resolver.resolve(user, client, null);

        // then
        assertThat(result.passed()).isFalse();
    }
}
