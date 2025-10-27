package com.zerobase.homemate.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        String secretKeyPath = System.getenv("FIREBASE_CREDENTIALS");
        if (secretKeyPath == null) {
            log.error("Firebase Credentials not set");
            return;
        }

        try (InputStream is = new FileInputStream(secretKeyPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(is))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            log.info("Firebase has been successfully initialized.");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
