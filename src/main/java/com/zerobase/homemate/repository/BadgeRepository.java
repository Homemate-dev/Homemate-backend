package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Badge;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.BadgeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    boolean existsByUserAndBadgeType(User user, BadgeType badgeType);

    List<Badge> findAllByUserId(Long userId);
}
