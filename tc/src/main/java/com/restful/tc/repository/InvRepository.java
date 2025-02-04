package com.restful.tc.repository;

import com.restful.tc.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findAll();
}
