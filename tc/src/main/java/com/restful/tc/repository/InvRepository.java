package com.restful.tc.repository;

import com.restful.tc.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;

import java.util.List;

public interface InvRepository extends JpaRepository<Invoice, Long> {
    //    List<Invoice> findAll();
    List<Invoice> findFirst5ByOrderByNoInvAsc();

    @Query(value = "WITH FilteredData AS (\n" +
            "    SELECT \n" +
            "        dt_inv,\n" +
            "        no_inv,\n" +
            "        no_cust,\n" +
            "        ROW_NUMBER() OVER (PARTITION BY no_cust ORDER BY no_inv DESC) AS rn\n" +
            "    FROM \n" +
            "        Invoice\n" +
            "    WHERE \n" +
            "        dt_inv = '2025-01-31'\n" +
            ")\n" +
            "SELECT TOP 10 \n" +
            "    dt_inv,\n" +
            "    no_inv,\n" +
            "    no_cust\n" +
            "FROM \n" +
            "    FilteredData\n" +
            "WHERE \n" +
            "    rn = 1\n" +
            "ORDER BY \n" +
            "    no_inv DESC;\n", nativeQuery = true)
    List<Invoice> findDistinctNoCustOrderByDtInvDesc();
}
