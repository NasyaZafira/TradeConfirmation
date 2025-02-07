package com.restful.tc.repository;

import com.restful.tc.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
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
    List<Invoice> finddistinct10Nocustorderbydtinvdesc();

    @Query(value = "SELECT dt_inv, no_inv, no_cust\n" +
            "FROM invoice  \n" +
            "WHERE dt_inv >= CAST(GETDATE() AS DATE) AND dt_inv < DATEADD(day, 1, CAST(GETDATE() AS DATE))\n" +
            "AND no_cust IN (\n" +
            "    SELECT no_cust\n" +
            "    FROM invoice\n" +
            "    WHERE dt_inv >= CAST(GETDATE() AS DATE) AND dt_inv < DATEADD(day, 1, CAST(GETDATE() AS DATE))\n" +
            "    GROUP BY no_cust\n" +
            "    HAVING COUNT(*) = 1\n" +
            ");", nativeQuery = true)
    List<Invoice> findDistinctNoCustByToday();
}
