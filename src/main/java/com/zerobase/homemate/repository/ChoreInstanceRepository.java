package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.ChoreInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoreInstanceRepository extends JpaRepository<ChoreInstance, Long> {
    
}
