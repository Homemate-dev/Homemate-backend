package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Notice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findByScheduledAtBefore(LocalDateTime scheduledAt, Pageable pageable);
}
