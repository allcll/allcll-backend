package kr.allcll.backend.domain.graduation.check.cert;

import kr.allcll.backend.support.graduation.GraduationHtmlParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GraduationClassicsCertFetcherParsingTest {

    private final GraduationHtmlParser parser = new GraduationHtmlParser();

    @DisplayName("사용자 정보의 인증여부 텍스트를 파싱한다")
    @Test
    void selectClassicsPassText() {
        // given
        int PASS_STATUS_INDEX = 0;
        String html = """
                <div class="b-con-box">
                    <h4 class="b-h4-tit01">사용자 정보</h4>
                    <div class="table-wrap">
                        <table class="b-board-table">
                            <tbody>
                                <tr>
                                    <th class="td-left" scope="row">인증여부</th>
                                    <td class="td-left">
                
                
                
                
                
                                    								2024년도 1학기
                                    								인증
                
                
                                   </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
                """;

        Document document = Jsoup.parse(html);

        // when
        String[] passTextParts = parser.selectClassicsPassText(document);
        System.out.println(passTextParts.length);
        System.out.println(passTextParts[0]);
        System.out.println(passTextParts[1]);

        String passText = passTextParts[PASS_STATUS_INDEX];
        boolean expect = !passText.equals("아니오");

        // then
        assertThat(expect).isEqualTo(true);
    }

    @DisplayName("고전특강 대상자의 인증여부가 아니오이면 false를 반환한다")
    @Test
    void selectClassicsPassText_notPassed_altcourse() {
        // given
        int PASS_STATUS_INDEX = 0;

        String html = """
                <div class="co-board">
                    <div class="bn-view-common type01">
                        <div class="b-con-box">
                            <h4 class="b-h4-tit01">사용자 정보</h4>
                            <div class="table-wrap">
                                <table class="b-board-table">
                                    <tbody>
                                        <tr>
                                            <th class="td-left" scope="row">인증여부</th>
                                            <td class="td-left">
                
                
                                            									아니오(고전특강 대상자)
                
                
                
                
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
                """;

        Document document = Jsoup.parse(html);

        // when
        String[] passTextParts = parser.selectClassicsPassText(document);
        System.out.println(passTextParts.length);
        System.out.println(passTextParts[0]);
        System.out.println(passTextParts[0]);

        String passText = passTextParts[PASS_STATUS_INDEX];
        boolean result = !passText.equals("아니오");

        // then
        assertThat(result).isFalse();
    }

    @DisplayName("인증여부가 아니오이면 false를 반환한다")
    @Test
    void selectClassicsPassText_notPassed() {
        // given
        int PASS_STATUS_INDEX = 0;

        String html = """
                <div class="co-board">
                    <div class="bn-view-common type01">
                        <div class="b-con-box">
                            <h4 class="b-h4-tit01">사용자 정보</h4>
                            <div class="table-wrap">
                                <table class="b-board-table">
                                    <tbody>
                                        <tr>
                                            <th class="td-left" scope="row">인증여부</th>
                                            <<td class="td-left">
                
                
                                             									아니오
                
                
                
                
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
                """;

        Document document = Jsoup.parse(html);

        // when
        String[] passTextParts = parser.selectClassicsPassText(document);
        System.out.println(passTextParts.length);
        System.out.println(passTextParts[0]);

        String passText = passTextParts[PASS_STATUS_INDEX];
        boolean result = !passText.equals("아니오");

        // then
        assertThat(result).isFalse();
    }
}
