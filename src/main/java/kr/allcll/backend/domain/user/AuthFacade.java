package kr.allcll.backend.domain.user;

import kr.allcll.backend.domain.graduation.check.cert.GraduationCertFetcher;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCertService;
import kr.allcll.backend.domain.graduation.check.cert.dto.GraduationCertInfo;
import kr.allcll.backend.domain.user.dto.LoginRequest;
import kr.allcll.backend.domain.user.dto.LoginResult;
import kr.allcll.backend.domain.user.dto.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthService authService;
    private final ToscAuthService toscAuthService;
    private final UserFetcher userFetcher;
    private final GraduationCertFetcher graduationCertFetcher;
    private final UserService userService;
    private final GraduationCertService graduationCertService;

    @Transactional
    public LoginResult login(LoginRequest loginRequest) {
        OkHttpClient client = authService.login(loginRequest);
        toscAuthService.loginTosc(client, loginRequest);

        UserInfo userInfo = userFetcher.fetch(client);
        User user = userService.findOrCreate(userInfo);

        try {
            GraduationCertInfo certInfo = graduationCertFetcher.fetch(client);
            graduationCertService.createOrUpdate(user, certInfo);
        } catch (Exception exception) {
            log.error("[GraduationCert] 인증 정보 저장 중 오류 발생 (유저 ID: {}): {}", user.getId(), exception.getMessage());
        }

        return new LoginResult(user.getId());
    }
}
