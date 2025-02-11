package com.restful.tc.repository;

import com.restful.tc.model.Executed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecutedRepository extends JpaRepository<Executed, String> {
    Executed findByNoCust(String noCust); // Sesuaikan dengan kolom yang relevan
}

