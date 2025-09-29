package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.enums.ChoreStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ChoreInstanceRepository extends JpaRepository<ChoreInstance, Long> {
    
    List<ChoreInstance> findByChoreIdAndDueDateGreaterThanEqualAndChoreStatus(
        Long choreId, 
        LocalDate dueDate, 
        ChoreStatus choreStatus
    );
}
