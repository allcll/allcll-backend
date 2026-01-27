package kr.allcll.backend.domain.user;

import java.io.IOException;
import kr.allcll.backend.domain.user.dto.LoginResult;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginFacade {

    private final AuthService authService;
    private final UserFetcher userFetcher;
    private final UserService userService;

    public LoginResult login(String id, String pw) throws IOException {
        OkHttpClient client = authService.login(id, pw);

        UserInfo info = userFetcher.fetch(client);

        User user = userService.findOrCreate(info);

        return new LoginResult(user.getId());
    }
}
