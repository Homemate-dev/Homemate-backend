package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Chores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoresRepository extends JpaRepository<Chores, Long> {

}
