package kr.allcll.backend.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.security.GeneralSecurityException;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.sheet.GraduationSheetsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(GraduationSheetsProperties.class)
public class GoogleSheetsConfig {

    private final GraduationSheetsProperties graduationSheetsProperties;
    private final ResourceLoader resourceLoader;

    @Bean
    public Sheets sheets() throws IOException, GeneralSecurityException {
        Resource key = resourceLoader.getResource(graduationSheetsProperties.credentialsLocation());

        if (!key.exists() || !key.isReadable()) {
            throw new AllcllException(AllcllErrorCode.GOOGLE_KEY_NOT_FOUND);
        }

        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(key.getInputStream());

        return new Sheets.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            JacksonFactory.getDefaultInstance(),
            new HttpCredentialsAdapter(googleCredentials)
        ).setApplicationName(graduationSheetsProperties.applicationName()).build();
    }
}
