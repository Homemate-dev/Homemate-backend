package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.ChoreInstances;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoreInstancesRepository extends JpaRepository<ChoreInstances, Long> {

}
