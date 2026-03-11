package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoriesRepository extends JpaRepository<Categories, Long> {

    List<Categories> findActiveMonthlyByTargetMonth(String targetMonth);

    @Modifying(clearAutomatically = true)
    @Query("""
update Categories c
set c.isActive = false
where c.targetMonth = :targetMonth
""")
    void deactivateAllCategories(String targetMonth);

    @Modifying(clearAutomatically = true)
    @Query("""
update Categories c
set c.isActive = true
where c.targetMonth = :targetMonth
""")
    void activateThisCategories(String targetMonth);

    long countByTargetMonth(String string);

    boolean existsByTargetMonthAndIsActiveTrue(String string);
}
