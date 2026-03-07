package kr.allcll.backend.domain.graduation.check.cert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import kr.allcll.backend.client.LoginProperties;
import kr.allcll.backend.domain.graduation.check.cert.dto.ClassicsCounts;
import kr.allcll.backend.domain.graduation.check.cert.dto.ClassicsResult;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.graduation.GraduationHtmlParser;
import okhttp3.OkHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("GraduationClassicsCertFetcher 테스트")
@ExtendWith(MockitoExtension.class)
class GraduationClassicsCertFetcherTest {

    @Mock
    private LoginProperties loginProperties;

    @Mock
    private GraduationHtmlParser parser;

    @Mock
    private GraduationCertDocumentFetcher documentFetcher;

    @Mock
    private OkHttpClient client;

    @InjectMocks
    private GraduationClassicsCertFetcher fetcher;

    @DisplayName("\"X권\" 형태의 HTML을 파싱하여 완료된 권수를 추출한다")
    @Test
    void parseCounts_withCountFormat() {
        // given
        String html = """
            <html>
                <body>
                    <div class="b-con-box">
                        <h4 class="b-h4-tit01">사용자 정보</h4>
                        <table>
                            <tbody>
                                <tr>
                                    <th>인증여부</th>
                                    <td>예</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="b-con-box">
                        <h4 class="b-h4-tit01">영역별 인증현황</h4>
                        <table class="b-board-table">
                            <tbody>
                                <tr>
                                    <th>서양의 역사와 사상</th>
                                    <td>4권</td>
                                </tr>
                                <tr>
                                    <th>동양의 역사와 사상</th>
                                    <td>2권</td>
                                </tr>
                                <tr>
                                    <th>동·서양의 문학</th>
                                    <td>2권</td>
                                </tr>
                                <tr>
                                    <th>과학 사상</th>
                                    <td>1권</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </body>
            </html>
            """;

        Document document = Jsoup.parse(html);
        given(loginProperties.studentInfoPageUrl()).willReturn("http://test.com");
        given(documentFetcher.fetch(any(), anyString(), any())).willReturn(document);
        given(parser.selectClassicsPassText(any())).willReturn("예");
        given(parser.selectClassicsDetailTable(any()))
            .willReturn(document.selectFirst(".b-con-box:has(h4.b-h4-tit01:contains(영역별 인증현황)) table.b-board-table"));

        // when
        ClassicsResult result = fetcher.fetchClassics(client);

        // then
        assertThat(result.passed()).isTrue();
        ClassicsCounts counts = result.counts();
        assertThat(counts.myCountWestern()).isEqualTo(4);
        assertThat(counts.myCountEastern()).isEqualTo(2);
        assertThat(counts.myCountEasternAndWestern()).isEqualTo(2);
        assertThat(counts.myCountScience()).isEqualTo(1);
        // totalMyCount는 최대 인정 권수로 제한 (4 + 2 + 2 + 1 = 9)
        assertThat(counts.totalMyCount()).isEqualTo(9);
    }

    @DisplayName("인증 미완료 상태의 HTML을 정상적으로 파싱한다")
    @Test
    void parseCounts_notPassed() {
        // given
        String html = """
            <html>
                <body>
                    <div class="b-con-box">
                        <h4 class="b-h4-tit01">사용자 정보</h4>
                        <table>
                            <tbody>
                                <tr>
                                    <th>인증여부</th>
                                    <td>아니오</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="b-con-box">
                        <h4 class="b-h4-tit01">영역별 인증현황</h4>
                        <table class="b-board-table">
                            <tbody>
                                <tr>
                                    <th>서양의 역사와 사상</th>
                                    <td>3권</td>
                                </tr>
                                <tr>
                                    <th>동양의 역사와 사상</th>
                                    <td>2권</td>
                                </tr>
                                <tr>
                                    <th>동·서양의 문학</th>
                                    <td>1권</td>
                                </tr>
                                <tr>
                                    <th>과학 사상</th>
                                    <td>0권</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </body>
            </html>
            """;

        Document document = Jsoup.parse(html);
        given(loginProperties.studentInfoPageUrl()).willReturn("http://test.com");
        given(documentFetcher.fetch(any(), anyString(), any())).willReturn(document);
        given(parser.selectClassicsPassText(any())).willReturn("아니오");
        given(parser.selectClassicsDetailTable(any()))
            .willReturn(document.selectFirst(".b-con-box:has(h4.b-h4-tit01:contains(영역별 인증현황)) table.b-board-table"));

        // when
        ClassicsResult result = fetcher.fetchClassics(client);

        // then
        assertThat(result.passed()).isFalse();
        ClassicsCounts counts = result.counts();
        assertThat(counts.myCountWestern()).isEqualTo(3);
        assertThat(counts.myCountEastern()).isEqualTo(2);
        assertThat(counts.myCountEasternAndWestern()).isEqualTo(1);
        assertThat(counts.myCountScience()).isZero();
    }

    @DisplayName("알 수 없는 영역은 무시하고 파싱을 계속한다")
    @Test
    void parseCounts_ignoreUnknownArea() {
        // given
        String html = """
            <html>
                <body>
                    <div class="b-con-box">
                        <h4 class="b-h4-tit01">사용자 정보</h4>
                        <table>
                            <tbody>
                                <tr>
                                    <th>인증여부</th>
                                    <td>예</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="b-con-box">
                        <h4 class="b-h4-tit01">영역별 인증현황</h4>
                        <table class="b-board-table">
                            <tbody>
                                <tr>
                                    <th>서양의 역사와 사상</th>
                                    <td>4권</td>
                                </tr>
                                <tr>
                                    <th>알 수 없는 영역</th>
                                    <td>99권</td>
                                </tr>
                                <tr>
                                    <th>동양의 역사와 사상</th>
                                    <td>2권</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </body>
            </html>
            """;

        Document document = Jsoup.parse(html);
        given(loginProperties.studentInfoPageUrl()).willReturn("http://test.com");
        given(documentFetcher.fetch(any(), anyString(), any())).willReturn(document);
        given(parser.selectClassicsPassText(any())).willReturn("예");
        given(parser.selectClassicsDetailTable(any()))
            .willReturn(document.selectFirst(".b-con-box:has(h4.b-h4-tit01:contains(영역별 인증현황)) table.b-board-table"));

        // when
        ClassicsResult result = fetcher.fetchClassics(client);

        // then
        assertThat(result.passed()).isTrue();
        ClassicsCounts counts = result.counts();
        assertThat(counts.myCountWestern()).isEqualTo(4);
        assertThat(counts.myCountEastern()).isEqualTo(2);
        assertThat(counts.myCountEasternAndWestern()).isZero();
        assertThat(counts.myCountScience()).isZero();
    }

    @DisplayName("영역별 인증현황 테이블이 없으면 예외를 발생시킨다")
    @Test
    void parseCounts_noTable() {
        // given
        String html = """
            <html>
                <body>
                    <div class="b-con-box">
                        <h4 class="b-h4-tit01">사용자 정보</h4>
                        <table>
                            <tbody>
                                <tr>
                                    <th>인증여부</th>
                                    <td>예</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </body>
            </html>
            """;

        Document document = Jsoup.parse(html);
        given(loginProperties.studentInfoPageUrl()).willReturn("http://test.com");
        given(documentFetcher.fetch(any(), anyString(), any())).willReturn(document);
        given(parser.selectClassicsPassText(any())).willReturn("예");
        given(parser.selectClassicsDetailTable(any())).willReturn(null);

        // when & then
        assertThatThrownBy(() -> fetcher.fetchClassics(client))
            .isInstanceOf(AllcllException.class);
    }

    @DisplayName("숫자가 아닌 값이 있으면 0으로 처리한다")
    @Test
    void parseCounts_invalidNumber() {
        // given
        String html = """
            <html>
                <body>
                    <div class="b-con-box">
                        <h4 class="b-h4-tit01">사용자 정보</h4>
                        <table>
                            <tbody>
                                <tr>
                                    <th>인증여부</th>
                                    <td>예</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="b-con-box">
                        <h4 class="b-h4-tit01">영역별 인증현황</h4>
                        <table class="b-board-table">
                            <tbody>
                                <tr>
                                    <th>서양의 역사와 사상</th>
                                    <td>N/A</td>
                                </tr>
                                <tr>
                                    <th>동양의 역사와 사상</th>
                                    <td>-</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </body>
            </html>
            """;

        Document document = Jsoup.parse(html);
        given(loginProperties.studentInfoPageUrl()).willReturn("http://test.com");
        given(documentFetcher.fetch(any(), anyString(), any())).willReturn(document);
        given(parser.selectClassicsPassText(any())).willReturn("예");
        given(parser.selectClassicsDetailTable(any()))
            .willReturn(document.selectFirst(".b-con-box:has(h4.b-h4-tit01:contains(영역별 인증현황)) table.b-board-table"));

        // when
        ClassicsResult result = fetcher.fetchClassics(client);

        // then
        ClassicsCounts counts = result.counts();
        assertThat(counts.myCountWestern()).isZero();
        assertThat(counts.myCountEastern()).isZero();
    }

    @DisplayName("최대 인정 권수를 초과한 경우 실제 값을 저장하고 totalMyCount는 최대값으로 계산한다")
    @Test
    void parseCounts_limitsToMaxRecognizedCount() {
        // given
        String html = """
            <html>
                <body>
                    <div class="b-con-box">
                        <h4 class="b-h4-tit01">사용자 정보</h4>
                        <table>
                            <tbody>
                                <tr>
                                    <th>인증여부</th>
                                    <td>예</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="b-con-box">
                        <h4 class="b-h4-tit01">영역별 인증현황</h4>
                        <table class="b-board-table">
                            <tbody>
                                <tr>
                                    <th>서양의 역사와 사상</th>
                                    <td>5권</td>
                                </tr>
                                <tr>
                                    <th>동양의 역사와 사상</th>
                                    <td>3권</td>
                                </tr>
                                <tr>
                                    <th>동·서양의 문학</th>
                                    <td>4권</td>
                                </tr>
                                <tr>
                                    <th>과학 사상</th>
                                    <td>2권</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </body>
            </html>
            """;

        Document document = Jsoup.parse(html);
        given(loginProperties.studentInfoPageUrl()).willReturn("http://test.com");
        given(documentFetcher.fetch(any(), anyString(), any())).willReturn(document);
        given(parser.selectClassicsPassText(any())).willReturn("예");
        given(parser.selectClassicsDetailTable(any()))
            .willReturn(document.selectFirst(".b-con-box:has(h4.b-h4-tit01:contains(영역별 인증현황)) table.b-board-table"));

        // when
        ClassicsResult result = fetcher.fetchClassics(client);

        // then
        ClassicsCounts counts = result.counts();
        // 실제 인증 권수는 그대로 저장
        assertThat(counts.myCountWestern()).isEqualTo(5);
        assertThat(counts.myCountEastern()).isEqualTo(3);
        assertThat(counts.myCountEasternAndWestern()).isEqualTo(4);
        assertThat(counts.myCountScience()).isEqualTo(2);
        // totalMyCount는 최대 인정 권수로 제한 (4 + 2 + 3 + 1 = 10)
        assertThat(counts.totalMyCount()).isEqualTo(10);
    }
}
