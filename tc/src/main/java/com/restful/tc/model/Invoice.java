package com.restful.tc.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "invoice")
public class Invoice {
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "no_inv")
    private String no_inv;

    @Column(name = "no_cust")
    private String no_cust;

    @Column(name = "dt_inv")
    private LocalDate dt_inv;

    public String getNoInvoice() {
        return no_inv;
    }
    public String getNoCust() {
        return no_cust;
    }
    public LocalDate getDate() {
        return dt_inv;
    }

    public Invoice(LocalDate dt_inv, Long id, String no_inv, String no_cust) {
        this.dt_inv = dt_inv;
        this.id = id;
        this.no_inv = no_inv;
        this.no_cust = no_cust;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDt_inv(LocalDate dt_inv) {
        this.dt_inv = dt_inv;
    }

    public void setNo_inv(String no_inv) {
        this.no_inv = no_inv;
    }

    public void setNo_cust(String no_cust) {
        this.no_cust = no_cust;
    }
}