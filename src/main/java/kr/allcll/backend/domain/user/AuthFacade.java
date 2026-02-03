package kr.allcll.backend.domain.user;

import kr.allcll.backend.domain.graduation.check.cert.GraduationCertFetcher;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCertService;
import kr.allcll.backend.domain.graduation.check.cert.dto.GraduationCertInfo;
import kr.allcll.backend.domain.user.dto.LoginRequest;
import kr.allcll.backend.domain.user.dto.LoginResult;
import kr.allcll.backend.domain.user.dto.UserInfo;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthService authService;
    private final UserFetcher userFetcher;
    private final GraduationCertFetcher graduationCertFetcher;
    private final UserService userService;
    private final GraduationCertService graduationCertService;

    public LoginResult login(LoginRequest loginRequest) {
        OkHttpClient client = authService.login(loginRequest);

        UserInfo userInfo = userFetcher.fetch(client);
        User user = userService.findOrCreate(userInfo);

        GraduationCertInfo certInfo = graduationCertFetcher.fetch(client);
        graduationCertService.createOrUpdate(user, certInfo);

        return new LoginResult(user.getId());
    }
}
