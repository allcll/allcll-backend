package kr.allcll.backend.domain.user;

import kr.allcll.backend.domain.user.dto.UserResponse;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findOrCreate(UserInfo info) {
        return userRepository.findByStudentId(info.studentId())
            .orElseGet(() -> save(info));
    }

    private User save(UserInfo info) {
        User user = User.of(
            info.studentId(),
            info.name(),
            info.deptNm()
        );
        return userRepository.save(user);
    }

    public UserResponse getResult(Long userId) {
        if (userId == null) {
            throw new AllcllException(AllcllErrorCode.UNAUTHORIZED_ACCESS);
        }
        User user = getById(userId);
        return UserResponse.from(user);
    }

    public User getById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.USER_NOT_FOUND));
    }
}
