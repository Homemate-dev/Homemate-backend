package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.CategoryChore;
import com.zerobase.homemate.entity.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryChoreRepository extends JpaRepository<CategoryChore, Long> {

    Page<CategoryChore> findByCategory(Category category, Pageable pageable);
}
