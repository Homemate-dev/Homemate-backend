package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

@Repository
public interface ChoreRepository extends JpaRepository<Chore, Long> {



    List<Chore> findTop4ByCategory(Category category);
}
