package com.zerobase.homemate.auth.support;

import com.zerobase.homemate.repository.UserRepository;
import com.zerobase.homemate.repository.UserSocialAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonalDataDeletePolicy {

    private static final int HOLD_POLICY = 14; // 2주(14일)
    private final UserRepository userRepository;
    private final UserSocialAccountRepository userSocialAccountRepository;

    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    public void deletePersonalData() {
        log.info("Deleting personal data...");
        LocalDateTime threshold = LocalDateTime.now().minusDays(HOLD_POLICY);

        List<Long> ids = userRepository.findIdsToDelete(threshold);
        if (ids.isEmpty()) {
            return;
        }

        userRepository.deleteProfileData(ids);
        userSocialAccountRepository.deleteSocialAccountData(ids);
        log.info("Delete personal data of {} users", ids.size());
    }
}
