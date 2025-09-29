package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.MonthlyCategoryTrend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MonthlyCategoryTrendRepository extends JpaRepository<MonthlyCategoryTrend, Long> {

    @Query("SELECT t FROM MonthlyCategoryTrend t WHERE t.yyyymm = :yyyymm ORDER BY t.registerCount DESC")
    List<MonthlyCategoryTrend> findTopNByYyyymmOrderByRegisterCountDesc(@Param("yyyymm") String yyyymm, int limit);
}
