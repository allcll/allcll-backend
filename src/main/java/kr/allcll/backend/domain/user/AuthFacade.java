package kr.allcll.backend.domain.user;

import kr.allcll.backend.domain.user.dto.LoginRequest;
import kr.allcll.backend.domain.user.dto.LoginResult;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthService authService;
    private final UserFetcher userFetcher;
    private final UserService userService;

    public LoginResult login(LoginRequest loginRequest) {
        OkHttpClient client = authService.login(loginRequest);

        UserInfo info = userFetcher.fetch(client);

        User user = userService.findOrCreate(info);

        return new LoginResult(user.getId());
    }
}
