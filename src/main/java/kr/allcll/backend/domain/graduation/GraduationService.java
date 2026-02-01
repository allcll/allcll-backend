package kr.allcll.backend.domain.graduation;

import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GraduationService {

    private final UserService userService;

    public GraduationResponse getResult(Long userId) {
        User user = userService.getById(userId);
        return GraduationResponse.from(user);
    }
}
