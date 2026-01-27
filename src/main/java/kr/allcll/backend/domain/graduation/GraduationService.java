package kr.allcll.backend.domain.graduation;

import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserService;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GraduationService {

    private final UserService userService;

    public GraduationResponse getResult(Long userId) {
        if (userId == null) {
            throw new AllcllException(AllcllErrorCode.UNAUTHORIZED_ACCESS);
        }
        User user = userService.findById(userId);

        return GraduationResponse.from(user);
    }
}
