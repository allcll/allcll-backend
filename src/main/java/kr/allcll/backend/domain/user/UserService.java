package kr.allcll.backend.domain.user;

import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfoRepository;
import kr.allcll.backend.domain.user.dto.UpdateUserRequest;
import kr.allcll.backend.domain.user.dto.UserInfo;
import kr.allcll.backend.domain.user.dto.UserResponse;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private static final int YEAR_PREFIX = 2000;

    private final UserRepository userRepository;
    private final GraduationDepartmentInfoRepository departmentInfoRepository;

    @Transactional
    public User findOrCreate(UserInfo userInfo) {
        return userRepository.findByStudentId(userInfo.studentId())
            .orElseGet(() -> save(userInfo));
    }

    private User save(UserInfo userInfo) {
        int admissionYear = extractAdmissionYear(userInfo.studentId());
        GraduationDepartmentInfo departmentInfo = departmentInfoRepository.findByAdmissionYearAndDeptNm(admissionYear,
                userInfo.deptNm())
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.DEPARTMENT_NOT_FOUND, userInfo.deptNm()));
        User user = new User(
            userInfo.studentId(),
            userInfo.name(),
            extractAdmissionYear(userInfo.studentId()),
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
        validateUserId(userId);
        User user = getById(userId);
        return UserResponse.from(user);
    }

    public User getById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void update(Long userId, UpdateUserRequest updateUserRequest) {
        validateUserId(userId);
        User user = getById(userId);
        if (MajorType.SINGLE.equals(updateUserRequest.majorType())) {
            GraduationDepartmentInfo dept = departmentInfoRepository.findByAdmissionYearAndDeptNm(
                    user.getAdmissionYear(), updateUserRequest.deptNm())
                .orElseThrow(
                    () -> new AllcllException(AllcllErrorCode.DEPARTMENT_NOT_FOUND, updateUserRequest.deptNm()));
            user.updateSingleMajorUser(updateUserRequest, dept);
            return;
        }
        GraduationDepartmentInfo doubleDept = departmentInfoRepository.findByAdmissionYearAndDeptNm(
                user.getAdmissionYear(), updateUserRequest.doubleDeptNm())
            .orElseThrow(
                () -> new AllcllException(AllcllErrorCode.DEPARTMENT_NOT_FOUND, updateUserRequest.doubleDeptNm()));
        user.updateDoubleMajorUser(updateUserRequest, doubleDept);
    }

    @Transactional
    public void delete(Long userId) {
        validateUserId(userId);
        departmentInfoRepository.deleteById(userId);
        User user = getById(userId);
        userRepository.delete(user);
    }

    private void validateUserId(Long userId) {
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
