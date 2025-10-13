package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Notice;
import com.zerobase.homemate.entity.NoticeRead;
import com.zerobase.homemate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NoticeReadRepository extends JpaRepository<NoticeRead, Long> {

    Optional<NoticeRead> findByUserAndNotice(User user, Notice notice);

    List<NoticeRead> findByUserAndNoticeIn(User user, Collection<Notice> notices);
}
