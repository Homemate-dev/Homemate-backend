package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.UserRole;
import com.zerobase.homemate.entity.enums.UserStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
    SELECT user.id
    FROM User user
    WHERE user.userStatus = :status
      AND user.userRole = :userRole
    """)
    List<Long> findIdsByUserStatusAndUserRole(
        @Param("status") UserStatus status,
        @Param("userRole") UserRole userRole);
}
