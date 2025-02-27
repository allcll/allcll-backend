package kr.allcll.backend.domain.basket;

import static kr.allcll.backend.fixture.BasketFixture.createEmptyBasket;
import static kr.allcll.backend.fixture.SubjectFixture.createSubjectWithDepartmentCode;
import static kr.allcll.backend.fixture.SubjectFixture.createSubjectWithDepartmentInformation;
import static kr.allcll.backend.fixture.SubjectFixture.createSubjectWithEverytimeLectureId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import kr.allcll.backend.domain.basket.dto.BasketsEachSubject;
import kr.allcll.backend.domain.basket.dto.BasketsResponse;
import kr.allcll.backend.domain.basket.dto.EachDepartmentBasket;
import kr.allcll.backend.domain.basket.dto.SubjectBasketsResponse;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.subject.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class BasketServiceTest {

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private BasketService basketService;

    @BeforeEach
    void setUp() {
        basketRepository.deleteAllInBatch();
        subjectRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("전체 관심과목 기능의 정상 동작을 확인한다.")
    void getAllSubjects() {
        // given
        Subject subjectA = createSubjectWithDepartmentInformation(
            "과목A",
            "전자정보통신공학과",
            "3210",
            "001234",
            "001",
            "김수민"
        );
        Subject subjectB = createSubjectWithDepartmentInformation(
            "과목B",
            "에너지자원공학과",
            "3211",
            "004321",
            "002",
            "김수민"
        );
        subjectRepository.saveAll(List.of(subjectA, subjectB));
        basketRepository.save(createBasket(subjectA, "본교생", "전자정보통신공학과", 1));
        basketRepository.save(createBasket(subjectB, "본교생", "에너지자원공학과", 1));
        int expectedSize = 2;

        // when
        BasketsResponse allSubjects = basketService.findBasketsByCondition(null, null, null);

        // then
        assertThat(allSubjects.baskets()).hasSize(expectedSize)
            .extracting(
                BasketsEachSubject::subjectName,
                BasketsEachSubject::subjectCode,
                BasketsEachSubject::classCode,
                BasketsEachSubject::professorName,
                BasketsEachSubject::departmentName,
                BasketsEachSubject::departmentCode
            ).containsExactly(
                tuple("과목A", "001234", "001", "김수민", "전자정보통신공학과", "3210"),
                tuple("과목B", "004321", "002", "김수민", "에너지자원공학과", "3211")
            );
    }

    @Test
    @DisplayName("전체 관심과목 조회를 했을 시 각 과목 조회를 확인한다.")
    void getEachSubject() {
        // given
        Subject subjectA = createSubjectWithDepartmentInformation(
            "과목A",
            "전자정보통신공학과",
            "3210",
            "001234",
            "001",
            "김수민"
        );
        subjectRepository.save(subjectA);
        basketRepository.save(createBasket(subjectA, "본교생", "전자정보통신공학과", 1));
        int expectedSize = 1;

        // when
        BasketsResponse allSubjects = basketService.findBasketsByCondition(null, null, null);

        // then
        assertThat(allSubjects.baskets()).hasSize(expectedSize)
            .extracting(
                BasketsEachSubject::subjectName,
                BasketsEachSubject::subjectCode,
                BasketsEachSubject::classCode,
                BasketsEachSubject::professorName,
                BasketsEachSubject::departmentName,
                BasketsEachSubject::departmentCode
            ).containsExactly(
                tuple("과목A", "001234", "001", "김수민", "전자정보통신공학과", "3210")
            );
    }

    @Test
    @DisplayName("학과 코드로 검색했을 시 관심 과목 조회를 확인한다.")
    void departmentCodeSelect() {
        // given
        saveSubjectsAndBaskets();
        int expectedSize = 2;

        // when
        BasketsResponse basketsByCondition = basketService.findBasketsByCondition(
            "3210",
            null,
            null);

        // then
        assertThat(basketsByCondition.baskets()).hasSize(expectedSize)
            .extracting(
                BasketsEachSubject::subjectName,
                BasketsEachSubject::subjectCode,
                BasketsEachSubject::classCode,
                BasketsEachSubject::professorName,
                BasketsEachSubject::departmentName,
                BasketsEachSubject::departmentCode
            ).containsExactly(
                tuple("컴공 과목A", "001234", "001", "김보예", "컴퓨터공학과", "3210"),
                tuple("컴공 과목B", "004321", "001", "김보예", "컴퓨터공학과", "3210")
            );
    }

    @Test
    @DisplayName("교수명으로 검색했을 시 관심 과목 조회를 확인한다.")
    void professorSelect() {
        // given
        saveSubjectsAndBaskets();
        int expectedSize = 3;

        // when
        BasketsResponse basketsByCondition = basketService.findBasketsByCondition(
            null,
            "김보예",
            null);

        // then
        assertThat(basketsByCondition.baskets()).hasSize(expectedSize)
            .extracting(
                BasketsEachSubject::subjectName,
                BasketsEachSubject::subjectCode,
                BasketsEachSubject::classCode,
                BasketsEachSubject::professorName,
                BasketsEachSubject::departmentName,
                BasketsEachSubject::departmentCode
            ).containsExactly(
                tuple("컴공 과목A", "001234", "001", "김보예", "컴퓨터공학과", "3210"),
                tuple("컴공 과목B", "004321", "001", "김보예", "컴퓨터공학과", "3210"),
                tuple("소웨 과목D", "001234", "001", "김보예", "소프트웨어학과", "3211")
            );
    }

    @Test
    @DisplayName("과목명으로 검색했을 시 관심 과목 조회를 확인한다.")
    void subjectNameSelect() {
        // given
        saveSubjectsAndBaskets();
        int expectedSize = 1;

        // when
        BasketsResponse basketsByCondition = basketService.findBasketsByCondition(
            null,
            null,
            "컴공 과목A");

        // then
        assertThat(basketsByCondition.baskets()).hasSize(expectedSize)
            .extracting(
                BasketsEachSubject::subjectName,
                BasketsEachSubject::subjectCode,
                BasketsEachSubject::classCode,
                BasketsEachSubject::professorName,
                BasketsEachSubject::departmentName,
                BasketsEachSubject::departmentCode
            ).containsExactly(
                tuple("컴공 과목A", "001234", "001", "김보예", "컴퓨터공학과", "3210")
            );
    }

    @Test
    @DisplayName("과목명과 교수명으로 검색했을 시 관심 과목 조회를 확인한다.")
    void subjectNameAndProfessorSelect() {
        // given
        saveSubjectsAndBaskets();
        int expectedSize = 1;

        // when
        BasketsResponse basketsByCondition = basketService.findBasketsByCondition(
            null,
            "김보예",
            "컴공 과목A");

        // then
        assertThat(basketsByCondition.baskets()).hasSize(expectedSize)
            .extracting(
                BasketsEachSubject::subjectName,
                BasketsEachSubject::subjectCode,
                BasketsEachSubject::classCode,
                BasketsEachSubject::professorName,
                BasketsEachSubject::departmentName,
                BasketsEachSubject::departmentCode
            ).containsExactly(
                tuple("컴공 과목A", "001234", "001", "김보예", "컴퓨터공학과", "3210")
            );
    }

    @Test
    @DisplayName("과목명과 학과코드로 검색했을 시 관심 과목 조회를 확인한다.")
    void subjectNameAndDepartmentCodeSelect() {
        // given
        saveSubjectsAndBaskets();
        int expectedSize = 1;

        // when
        BasketsResponse basketsByCondition = basketService.findBasketsByCondition(
            "3210",
            null,
            "컴공 과목A");

        // then
        assertThat(basketsByCondition.baskets()).hasSize(expectedSize)
            .extracting(
                BasketsEachSubject::subjectName,
                BasketsEachSubject::subjectCode,
                BasketsEachSubject::classCode,
                BasketsEachSubject::professorName,
                BasketsEachSubject::departmentName,
                BasketsEachSubject::departmentCode
            ).containsExactly(
                tuple("컴공 과목A", "001234", "001", "김보예", "컴퓨터공학과", "3210")
            );
    }

    @Test
    @DisplayName("과목명과 학과코드와 교수명으로 검색했을 시 관심 과목 조회를 확인한다.")
    void subjectNameAndDepartmentCodeAndProfessorNameSelect() {
        // given
        saveSubjectsAndBaskets();
        int expectedSize = 1;

        // when
        BasketsResponse basketsByCondition = basketService.findBasketsByCondition(
            "3210",
            "김보예",
            "컴공 과목B");

        // then
        assertThat(basketsByCondition.baskets()).hasSize(expectedSize)
            .extracting(
                BasketsEachSubject::subjectName,
                BasketsEachSubject::subjectCode,
                BasketsEachSubject::classCode,
                BasketsEachSubject::professorName,
                BasketsEachSubject::departmentName,
                BasketsEachSubject::departmentCode
            ).containsExactly(
                tuple("컴공 과목B", "004321", "001", "김보예", "컴퓨터공학과", "3210")
            );
    }

    @Test
    @DisplayName("잘못된 조건으로 검색 시 아무것도 조회되지 않음을 확인한다.")
    void wrongSelect() {
        // given
        saveSubjectsAndBaskets();
        int expectedSize = 0;

        // when
        BasketsResponse basketsByCondition = basketService.findBasketsByCondition(
            "3211",
            "김보예",
            "컴공 과목B");

        // then
        assertThat(basketsByCondition.baskets()).hasSize(expectedSize);
    }

    @Test
    @DisplayName("각 과목에 대한 관심과목 조회를 확인한다.")
    void getEachSubjectBasketsTest() {
        // given
        Subject subjectA = createSubjectWithDepartmentCode("컴공 과목A", "001234", "001", "김보예", "3210");
        subjectRepository.save(subjectA);
        Basket basketA = createBasket(subjectA, "본교생", "컴공", 10);
        Basket basketB = createBasket(subjectA, "본교생", "전정통", 3);
        basketRepository.saveAll(List.of(basketA, basketB));
        int expected = 2;

        // when
        SubjectBasketsResponse eachSubjectBaskets = basketService.getEachSubjectBaskets(subjectA.getId());

        // then
        assertThat(eachSubjectBaskets.eachDepartmentRegisters()).hasSize(expected);
        assertThat(eachSubjectBaskets.eachDepartmentRegisters()).hasSize(expected)
            .extracting(
                EachDepartmentBasket::studentBelong,
                EachDepartmentBasket::registerDepartment,
                EachDepartmentBasket::eachCount
            ).containsExactly(
                tuple("본교생", "컴공", 10),
                tuple("본교생", "전정통", 3)
            );

    }

    @Test
    @DisplayName("각 과목에 대한 관심과목 조회의 응답값을 확인한다.")
    void getEachSubjectBasketResponse() {
        // given
        Subject subjectA = createSubjectWithDepartmentCode("컴공 과목A", "001234", "001", "김보예", "3210");
        subjectRepository.save(subjectA);
        Basket basketA = createBasket(subjectA, "본교생", "컴공", 10);
        basketRepository.save(basketA);

        // when
        SubjectBasketsResponse response = basketService.getEachSubjectBaskets(subjectA.getId());

        // thene
        assertThat(response.eachDepartmentRegisters()).hasSize(1)
            .extracting(
                EachDepartmentBasket::studentBelong,
                EachDepartmentBasket::registerDepartment,
                EachDepartmentBasket::eachCount
            ).containsExactly(
                tuple("본교생", "컴공", 10)
            );
    }

    @Test
    @DisplayName("과목에 대한 관심과목 담기한 인원이 없을 때는 빈 응답을 반환한다.")
    void emptyBasketResponse() {
        // given
        Subject subjectA = createSubjectWithDepartmentCode("컴공 과목A", "001234", "001", "김보예", "3210");
        subjectRepository.save(subjectA);
        Basket emptyBasket = createEmptyBasket(subjectA);
        basketRepository.save(emptyBasket);

        // when
        SubjectBasketsResponse response = basketService.getEachSubjectBaskets(subjectA.getId());

        // then
        assertThat(response.eachDepartmentRegisters()).isEmpty();
    }

    @Test
    @DisplayName("관심과목 인원이 0명 일 때의 동작을 확인한다.")
    void emptyBasket() {
        // given
        Subject subjectA = createSubjectWithDepartmentCode("컴공 과목A", "001234", "001", "김보예", "3210");
        subjectRepository.save(subjectA);

        // when
        BasketsResponse basketsByCondition = basketService.findBasketsByCondition(null, null, null);

        //then
        List<BasketsEachSubject> baskets = basketsByCondition.baskets();
        assertThat(baskets.getFirst().totalCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("관심과목 인원이 0명 일 때 과목 조회를 확인한다.")
    void emptyBasketSubject() {
        // given
        Subject subjectA = createSubjectWithDepartmentCode("컴공 과목A", "001234", "001", "김보예", "3210");
        subjectRepository.save(subjectA);

        // when
        SubjectBasketsResponse eachSubjectBaskets = basketService.getEachSubjectBaskets(subjectA.getId());

        // then
        assertThat(eachSubjectBaskets.eachDepartmentRegisters().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("관심과목 세부 조회에 에브리타임 강의 아이디를 확인한다.")
    void everytimeLectureIdTest() {
        // given
        long everytimeLectureId = 1234567L;
        Subject subjectA = createSubjectWithEverytimeLectureId(everytimeLectureId, "컴공 과목A", "001234", "001", "김보예",
            "3210");
        subjectRepository.save(subjectA);

        // when
        SubjectBasketsResponse eachSubjectBaskets = basketService.getEachSubjectBaskets(subjectA.getId());

        // then
        assertThat(eachSubjectBaskets.everytimeLectureId()).isEqualTo(1234567L);
    }

    private void saveSubjectsAndBaskets() {
        Subject subjectA = createSubjectWithDepartmentInformation(
            "컴공 과목A",
            "컴퓨터공학과",
            "3210",
            "001234",
            "001",
            "김보예"
        );

        Subject subjectB = createSubjectWithDepartmentInformation(
            "컴공 과목B",
            "컴퓨터공학과",
            "3210",
            "004321",
            "001",
            "김보예"
        );

        Subject subjectC = createSubjectWithDepartmentInformation(
            "소웨 과목C",
            "소프트웨어학과",
            "3211",
            "004321",
            "001",
            "김수민"
        );

        Subject subjectD = createSubjectWithDepartmentInformation(
            "소웨 과목D",
            "소프트웨어학과",
            "3211",
            "001234",
            "001",
            "김보예"
        );

        subjectRepository.saveAll(
            List.of(
                subjectA,
                subjectB,
                subjectC,
                subjectD
            )
        );
        Basket basketA = createBasket(subjectA, "본교생", "컴공", 10);
        Basket basketB = createBasket(subjectA, "본교생", "전정통", 3);
        Basket basketC = createBasket(subjectB, "본교생", "컴공", 4);
        Basket basketD = createBasket(subjectC, "본교생", "전정통", 5);
        Basket basketE = createBasket(subjectD, "본교생", "소웨", 6);

        basketRepository.saveAll(
            List.of(
                basketA,
                basketB,
                basketC,
                basketD,
                basketE
            )
        );
    }

    private Basket createBasket(
        Subject subject,
        String studentBelong,
        String registerDepartment,
        Integer eachCount
    ) {
        return new Basket(subject, "", "", "", "", "", studentBelong,
            "", registerDepartment, "", "", 200, 0, 0,
            0, 0, eachCount, 10);
    }
}
