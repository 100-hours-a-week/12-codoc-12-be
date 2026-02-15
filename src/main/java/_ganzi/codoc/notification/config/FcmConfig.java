package _ganzi.codoc.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@ConditionalOnProperty(prefix = "app.fcm", name = "enabled", havingValue = "true")
@Configuration
public class FcmConfig {

    @Bean
    @Nullable
    public FirebaseApp firebaseApp() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.getInstance();
            }

            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
            return FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            log.warn("Failed to initialize FCM. Push notifications are disabled.", e);
            return null;
        }
    }

    @Bean
    @Nullable
    public FirebaseMessaging firebaseMessaging(ObjectProvider<FirebaseApp> firebaseAppProvider) {
        FirebaseApp firebaseApp = firebaseAppProvider.getIfAvailable();
        if (firebaseApp == null) {
            log.info("FCM is unavailable. FirebaseMessaging bean will not be created.");
            return null;
        }

        try {
            return FirebaseMessaging.getInstance(firebaseApp);
        } catch (Exception e) {
            log.warn("Failed to create FirebaseMessaging. Push notifications are disabled.", e);
            return null;
        }
    }
}
