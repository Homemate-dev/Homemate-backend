package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.WithdrawLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WithdrawLogRepository extends JpaRepository<WithdrawLog, Long> {

    List<WithdrawLog> findByCreatedAtBetweenOrderByIdAsc(LocalDateTime from, LocalDateTime to);
}