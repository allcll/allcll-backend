package kr.allcll.backend.domain.user;

import kr.allcll.backend.domain.user.dto.UserResponse;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public User findOrCreate(UserInfo info) {
        return repository.findByStudentId(info.studentId())
            .orElseGet(() -> save(info));
    }

    private User save(UserInfo info) {
        User user = User.create(
            info.studentId(),
            info.name(),
            info.deptNm()
        );
        return repository.save(user);
    }

    public UserResponse getResult(Long userId) {
        if (userId == null) {
            throw new AllcllException(AllcllErrorCode.UNAUTHORIZED_ACCESS);
        }
        User user = findById(userId);
        return UserResponse.from(user);
    }

    public User findById(Long userId) {
        return repository.findById(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.USER_NOT_FOUND));
    }
}
