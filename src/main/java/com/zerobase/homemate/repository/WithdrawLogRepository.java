package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.WithdrawLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WithdrawLogRepository extends JpaRepository<WithdrawLog, Long> {

}