package com.restful.tc.repository;

import com.restful.tc.model.Subacc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubaccRepository extends JpaRepository<Subacc, String> {
    Subacc findByNoCust(String noCust);
}