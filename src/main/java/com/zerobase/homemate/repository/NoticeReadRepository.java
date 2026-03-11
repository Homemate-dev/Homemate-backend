package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Notice;
import com.zerobase.homemate.entity.NoticeRead;
import com.zerobase.homemate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoticeReadRepository extends JpaRepository<NoticeRead, Long> {

    Optional<NoticeRead> findByUserAndNotice(User user, Notice notice);

    @Query("""
            SELECT nr FROM NoticeRead nr
            WHERE nr.user.id = :userId
            AND nr.notice IN (:notices)
            """)
    List<NoticeRead> findNoticeReadHistory(
            @Param("userId") Long userId,
            @Param("notices") List<Notice> notices
    );
}
