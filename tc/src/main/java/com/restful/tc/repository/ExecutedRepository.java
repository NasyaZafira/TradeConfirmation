package com.restful.tc.repository;

import com.restful.tc.model.Executed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExecutedRepository extends JpaRepository<Executed, String> {
    List<Executed> findByNoCust(String noCust);// Sesuaikan dengan kolom yang relevan

    @Query("SELECT e FROM Executed e WHERE e.noCust = :noCust AND e.executionDate = :executionDate")
    List<Executed> findByNoCustAndExecutionDate(@Param("noCust") String noCust, @Param("executionDate") LocalDate executionDate);
}

