package kr.allcll.backend.domain.graduation.department;

import java.util.List;
import kr.allcll.backend.domain.graduation.department.dto.GraduationDepartmentResponse;
import kr.allcll.backend.domain.graduation.department.dto.GraduationDepartmentsResponse;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GraduationDepartmentService {

    private final UserRepository userRepository;
    private final GraduationDepartmentInfoRepository graduationDepartmentInfoRepository;

    public GraduationDepartmentsResponse getAllDepartments(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.USER_NOT_FOUND));
        int admissionYear = user.getAdmissionYear();

        List<GraduationDepartmentInfo> graduationDepartmentInfos = graduationDepartmentInfoRepository.findAllByAdmissionYear(admissionYear);

        List<GraduationDepartmentResponse> graduationDepartmentResponses = graduationDepartmentInfos.stream()
            .map(graduationDepartmentInfo -> new GraduationDepartmentResponse(
                graduationDepartmentInfo.getDeptCd(),
                graduationDepartmentInfo.getDeptNm(),
                graduationDepartmentInfo.getCollegeNm(),
                graduationDepartmentInfo.getDeptGroup().name(),
                graduationDepartmentInfo.getEnglishTargetType().name(),
                graduationDepartmentInfo.getCodingTargetType().name()
            ))
            .toList();
        return GraduationDepartmentsResponse.of(admissionYear, graduationDepartmentResponses);
    }
}
