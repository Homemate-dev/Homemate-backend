package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Chore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoreRepository extends JpaRepository<Chore, Long> {

}
