package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Mission;
import java.time.YearMonth;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {

    List<Mission> findByActiveYearMonthAndIsActiveTrueOrderByIdAsc(YearMonth yearMonth);
}
