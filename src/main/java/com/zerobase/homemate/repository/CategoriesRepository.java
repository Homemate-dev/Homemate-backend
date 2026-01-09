package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriesRepository extends JpaRepository<Categories, Long> {

    List<Categories> findActiveMonthlyByTargetMonth(String targetMonth);
}
