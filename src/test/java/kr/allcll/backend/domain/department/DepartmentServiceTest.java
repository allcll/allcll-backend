package kr.allcll.backend.domain.department;

import static org.assertj.core.api.Assertions.assertThat;

import kr.allcll.backend.domain.department.Department;
import kr.allcll.backend.domain.department.DepartmentRepository;
import kr.allcll.backend.domain.department.DepartmentService;
import kr.allcll.backend.domain.department.dto.DepartmentResponse;
import kr.allcll.backend.domain.department.dto.DepartmentsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class DepartmentServiceTest {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUp() {
        departmentRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("과목 코드 전체 조회의 정상 동작을 확인한다.")
    void retrieveAllDepartment() {
        // given
        departmentRepository.save(new Department("3210", "컴퓨터공학과 소프트웨어융합대학"));
        departmentRepository.save(new Department("2131", "국제학부 영어영문학전공"));
        int expected = 2;

        // when
        DepartmentsResponse response = departmentService.retrieveAllDepartment();

        // then
        assertThat(response.departments()).hasSize(expected);
    }

    @Test
    @DisplayName("과목 코드 전체 조회의 응답값을 확인한다.")
    void retrieveAllDepartmentResponse() {
        // given
        departmentRepository.save(new Department("3210", "컴퓨터공학과 소프트웨어융합대학"));

        // when
        DepartmentsResponse response = departmentService.retrieveAllDepartment();

        // then
        DepartmentResponse compareResponse = response.departments().getFirst();
        assertThat(compareResponse.departmentCode()).isEqualTo("3210");
        assertThat(compareResponse.departmentName()).isEqualTo("컴퓨터공학과 소프트웨어융합대학");
    }
}
