package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.FcmToken;
import com.zerobase.homemate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByToken(String token);

    List<FcmToken> findAllByUserAndIsActiveTrue(User user);
}
