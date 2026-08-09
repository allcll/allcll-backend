package kr.allcll.backend.domain.user;

import kr.allcll.backend.domain.graduation.check.cert.GraduationCertResolver;
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
    private final UserFetcher userFetcher;
    private final UserService userService;
    private final ToscAuthService toscAuthService;
    private final GraduationCertService graduationCertService;
    private final GraduationCertResolver graduationCertResolver;

    @Transactional
    public LoginResult login(LoginRequest loginRequest) {
        OkHttpClient client = authService.login(loginRequest);

        boolean toscLoginFailed = false;
        try {
            toscAuthService.loginTosc(loginRequest);
        } catch (Exception exception) {
            toscLoginFailed = true;
            log.error("[TOSC] 로그인 실패로 TOSC 정보 업데이트를 건너뜁니다. exceptionType={}",
                exception.getClass().getSimpleName());
        }

        UserInfo userInfo = userFetcher.fetch(client);
        User user = userService.findOrCreate(userInfo);

        try {
            GraduationCertInfo certInfo = graduationCertResolver.resolve(user, client);
            graduationCertService.createOrUpdate(user, certInfo);
        } catch (Exception exception) {
            log.error("[GraduationCert] 인증 정보 저장 실패. exceptionType={}",
                exception.getClass().getSimpleName());
        }

        return new LoginResult(user.getId(), toscLoginFailed);
    }
}
