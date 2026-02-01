package kr.allcll.backend.domain.user;

import kr.allcll.backend.domain.user.dto.LoginPatchRequest;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfoRepository;
import kr.allcll.backend.domain.user.dto.UserInfo;
import kr.allcll.backend.domain.user.dto.UserResponse;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int YEAR_PREFIX = 2000;

    private final UserRepository userRepository;
    private final GraduationDepartmentInfoRepository departmentInfoRepository;

    public User findOrCreate(UserInfo info) {
        return userRepository.findByStudentId(info.studentId())
            .orElseGet(() -> save(info));
    }

    private User save(UserInfo info) {
        GraduationDepartmentInfo departmentInfo = departmentInfoRepository.findByDeptNm(info.deptNm());
        User user = new User(
            info.studentId(),
            info.name(),
            extractAdmissionYear(info.studentId()),
            MajorType.SINGLE,
            departmentInfo.getCollegeNm(),
            departmentInfo.getDeptNm(),
            departmentInfo.getDeptCd(),
            null,
            null,
            null
        );
        return userRepository.save(user);
    }

    public UserResponse getResult(Long userId) {
        validateAuthenticatedUser(userId);
        User user = getById(userId);
        return UserResponse.from(user);
    }

    public User getById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.USER_NOT_FOUND));
    }


    public void update(Long userId, LoginPatchRequest loginPatchRequest) {
        validateAuthenticatedUser(userId);
    }

    private void validateAuthenticatedUser(Long userId) {
        if (userId == null) {
            throw new AllcllException(AllcllErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private int extractAdmissionYear(String studentId) {
        if (studentId == null) {
            throw new AllcllException(AllcllErrorCode.STUDENT_ID_FETCH_FAIL, studentId);
        }
        int year = Integer.parseInt(studentId.substring(0, 2));
        return YEAR_PREFIX + year;
    }
}
