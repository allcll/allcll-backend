package kr.allcll.backend.domain.subject;

import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SubjectApi.class)
class SubjectApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubjectService subjectService;

    @DisplayName("과목을 조회한다.")
    @Test
    void getSubjectsApiTest() throws Exception {
        mockMvc.perform(get("/api/subjects")
                .param("subjectId", "1")
                .param("subjectName", "컴퓨터구조"))
            .andExpect(status().isOk());

        then(subjectService).should().findSubjectsByCondition(1L, "컴퓨터구조", null, null, null);
    }
}
