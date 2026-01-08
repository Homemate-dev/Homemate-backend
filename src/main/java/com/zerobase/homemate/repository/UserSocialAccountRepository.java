package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.UserSocialAccount;
import com.zerobase.homemate.entity.enums.SocialProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSocialAccountRepository extends JpaRepository<UserSocialAccount, Long> {
  Optional<UserSocialAccount> findBySocialProviderAndProviderUserId(SocialProvider socialProvider, String providerUserId);

  Optional<UserSocialAccount> findByUser(User user);
}
