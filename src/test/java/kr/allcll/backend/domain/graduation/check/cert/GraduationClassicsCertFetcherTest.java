package kr.allcll.backend.domain.graduation.check.cert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import kr.allcll.backend.client.LoginProperties;
import kr.allcll.backend.domain.graduation.check.cert.dto.ClassicsResult;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.graduation.GraduationHtmlParser;
import okhttp3.OkHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraduationClassicsCertFetcherTest {

    @Mock
    private LoginProperties properties;

    @Mock
    private GraduationHtmlParser parser;

    @Mock
    private GraduationCertDocumentFetcher documentFetcher;

    @InjectMocks
    private GraduationClassicsCertFetcher classicsCertFetcher;

    private OkHttpClient client;
    private Document mockDocument;

    @BeforeEach
    void setUp() {
        client = new OkHttpClient();
        mockDocument = Jsoup.parse("<html><body></body></html>");
    }

    @Test
    @DisplayName("고전인증 합격 시 true를 반환한다")
    void fetchClassics_pass() {
        // given
        given(properties.studentInfoPageUrl()).willReturn("http://test.com");
        given(documentFetcher.fetch(any(OkHttpClient.class), eq("http://test.com"),
            eq(AllcllErrorCode.CLASSIC_INFO_FETCH_FAIL)))
            .willReturn(mockDocument);
        given(parser.selectClassicsPassText(mockDocument)).willReturn(new String[]{"예", "2024-01-01"});

        Document detailDoc = createMockClassicsDetailDocument(2, 2, 2, 2);
        given(parser.selectClassicsDetailTable(mockDocument)).willReturn(detailDoc.selectFirst("table"));

        // when
        ClassicsResult result = classicsCertFetcher.fetchClassics(client);

        // then
        assertThat(result.passed()).isTrue();
        assertThat(result.counts().myCountWestern()).isEqualTo(2);
        assertThat(result.counts().myCountEastern()).isEqualTo(2);
        assertThat(result.counts().myCountEasternAndWestern()).isEqualTo(2);
        assertThat(result.counts().myCountScience()).isEqualTo(2);
    }

    @Test
    @DisplayName("고전인증 불합격 시 false를 반환한다")
    void fetchClassics_fail() {
        // given
        given(properties.studentInfoPageUrl()).willReturn("http://test.com");
        given(documentFetcher.fetch(any(OkHttpClient.class), eq("http://test.com"),
            eq(AllcllErrorCode.CLASSIC_INFO_FETCH_FAIL)))
            .willReturn(mockDocument);
        given(parser.selectClassicsPassText(mockDocument)).willReturn(new String[]{"아니오", ""});

        Document detailDoc = createMockClassicsDetailDocument(1, 1, 1, 1);
        given(parser.selectClassicsDetailTable(mockDocument)).willReturn(detailDoc.selectFirst("table"));

        // when
        ClassicsResult result = classicsCertFetcher.fetchClassics(client);

        // then
        assertThat(result.passed()).isFalse();
        assertThat(result.counts().myCountWestern()).isEqualTo(1);
        assertThat(result.counts().myCountEastern()).isEqualTo(1);
        assertThat(result.counts().myCountEasternAndWestern()).isEqualTo(1);
        assertThat(result.counts().myCountScience()).isEqualTo(1);
    }

    @Test
    @DisplayName("고전 영역별 이수 권수가 0인 경우 0을 반환한다")
    void fetchClassics_noBooksCompleted() {
        // given
        given(properties.studentInfoPageUrl()).willReturn("http://test.com");
        given(documentFetcher.fetch(any(OkHttpClient.class), eq("http://test.com"),
            eq(AllcllErrorCode.CLASSIC_INFO_FETCH_FAIL)))
            .willReturn(mockDocument);
        given(parser.selectClassicsPassText(mockDocument)).willReturn(new String[]{"아니오", ""});

        Document detailDoc = createMockClassicsDetailDocument(0, 0, 0, 0);
        given(parser.selectClassicsDetailTable(mockDocument)).willReturn(detailDoc.selectFirst("table"));

        // when
        ClassicsResult result = classicsCertFetcher.fetchClassics(client);

        // then
        assertThat(result.passed()).isFalse();
        assertThat(result.counts().myCountWestern()).isEqualTo(0);
        assertThat(result.counts().myCountEastern()).isEqualTo(0);
        assertThat(result.counts().myCountEasternAndWestern()).isEqualTo(0);
        assertThat(result.counts().myCountScience()).isEqualTo(0);
    }

    @Test
    @DisplayName("고전특강 대상자인 경우에도 불합격으로 처리한다")
    void fetchClassics_failWithClassicsLectureTarget() {
        // given
        given(properties.studentInfoPageUrl()).willReturn("http://test.com");
        given(documentFetcher.fetch(any(OkHttpClient.class), eq("http://test.com"),
            eq(AllcllErrorCode.CLASSIC_INFO_FETCH_FAIL)))
            .willReturn(mockDocument);
        given(parser.selectClassicsPassText(mockDocument)).willReturn(new String[]{"아니오", "고전특강 대상자"});

        Document detailDoc = createMockClassicsDetailDocument(1, 1, 1, 1);
        given(parser.selectClassicsDetailTable(mockDocument)).willReturn(detailDoc.selectFirst("table"));

        // when
        ClassicsResult result = classicsCertFetcher.fetchClassics(client);

        // then
        assertThat(result.passed()).isFalse();
        assertThat(result.counts().myCountWestern()).isEqualTo(1);
        assertThat(result.counts().myCountEastern()).isEqualTo(1);
        assertThat(result.counts().myCountEasternAndWestern()).isEqualTo(1);
        assertThat(result.counts().myCountScience()).isEqualTo(1);
    }

    @Test
    @DisplayName("고전 상세 테이블이 null인 경우 예외를 발생시킨다")
    void fetchClassics_throwsWhenTableIsNull() {
        // given
        given(properties.studentInfoPageUrl()).willReturn("http://test.com");
        given(documentFetcher.fetch(any(OkHttpClient.class), eq("http://test.com"),
            eq(AllcllErrorCode.CLASSIC_INFO_FETCH_FAIL)))
            .willReturn(mockDocument);
        given(parser.selectClassicsPassText(mockDocument)).willReturn(new String[]{"아니오", ""});
        given(parser.selectClassicsDetailTable(mockDocument)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> classicsCertFetcher.fetchClassics(client))
            .isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.CLASSIC_DETAIL_INFO_FETCH_FAIL.getMessage());
    }

    @Test
    @DisplayName("공백이 포함된 인증 텍스트를 정상적으로 처리한다")
    void fetchClassics_withWhitespace() {
        // given
        given(properties.studentInfoPageUrl()).willReturn("http://test.com");
        given(documentFetcher.fetch(any(OkHttpClient.class), eq("http://test.com"),
            eq(AllcllErrorCode.CLASSIC_INFO_FETCH_FAIL)))
            .willReturn(mockDocument);
        // 공백이 포함된 텍스트 (trim 전제)
        given(parser.selectClassicsPassText(mockDocument)).willReturn(new String[]{"  예  ", "2024-01-01"});

        Document detailDoc = createMockClassicsDetailDocument(2, 2, 2, 2);
        given(parser.selectClassicsDetailTable(mockDocument)).willReturn(detailDoc.selectFirst("table"));

        // when
        ClassicsResult result = classicsCertFetcher.fetchClassics(client);

        // then
        assertThat(result.passed()).isTrue();
    }

    @Test
    @DisplayName("숫자 파싱 실패 시 0을 반환한다")
    void fetchClassics_invalidNumberFormat() {
        // given
        given(properties.studentInfoPageUrl()).willReturn("http://test.com");
        given(documentFetcher.fetch(any(OkHttpClient.class), eq("http://test.com"),
            eq(AllcllErrorCode.CLASSIC_INFO_FETCH_FAIL)))
            .willReturn(mockDocument);
        given(parser.selectClassicsPassText(mockDocument)).willReturn(new String[]{"아니오", ""});

        // 잘못된 숫자 형식 포함
        Document detailDoc = createMockClassicsDetailDocumentWithInvalidNumber();
        given(parser.selectClassicsDetailTable(mockDocument)).willReturn(detailDoc.selectFirst("table"));

        // when
        ClassicsResult result = classicsCertFetcher.fetchClassics(client);

        // then
        assertThat(result.counts().myCountWestern()).isEqualTo(0);
    }

    private Document createMockClassicsDetailDocument(int western, int eastern, int literature, int science) {
        String html = String.format("""
            <table class="b-board-table">
                <tbody>
                    <tr>
                        <th>서양의 역사와 사상</th>
                        <td>%d권</td>
                    </tr>
                    <tr>
                        <th>동양의 역사와 사상</th>
                        <td>%d권</td>
                    </tr>
                    <tr>
                        <th>동·서양의 문학</th>
                        <td>%d권</td>
                    </tr>
                    <tr>
                        <th>과학 사상</th>
                        <td>%d권</td>
                    </tr>
                </tbody>
            </table>
            """, western, eastern, literature, science);
        return Jsoup.parse(html);
    }

    private Document createMockClassicsDetailDocumentWithInvalidNumber() {
        String html = """
            <table class="b-board-table">
                <tbody>
                    <tr>
                        <th>서양의 역사와 사상</th>
                        <td>잘못된값</td>
                    </tr>
                    <tr>
                        <th>동양의 역사와 사상</th>
                        <td>1권</td>
                    </tr>
                    <tr>
                        <th>동·서양의 문학</th>
                        <td>1권</td>
                    </tr>
                    <tr>
                        <th>과학 사상</th>
                        <td>1권</td>
                    </tr>
                </tbody>
            </table>
            """;
        return Jsoup.parse(html);
    }
}
