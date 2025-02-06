package com.restful.tc.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "invoice")
public class Invoice {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    @Column(name = "no_inv")
    private String noInv;

    @Column(name = "no_cust")
    private String no_cust;

    @Column(name = "dt_inv")
    private LocalDate dt_inv;

    public Invoice() {
    }

    public String getNoInvoice() {
        return noInv;
    }
    public String getNoCust() {
        return no_cust;
    }
    public LocalDate getDate() {
        return dt_inv;
    }

    public Invoice(LocalDate dt_inv, String no_inv, String no_cust) {
        this.dt_inv = dt_inv;

        this.noInv = no_inv;
        this.no_cust = no_cust;
    }


    public void setDt_inv(LocalDate dt_inv) {
        this.dt_inv = dt_inv;
    }

    public void setNoInv(String noInv) {
        this.noInv = noInv;
    }

    public void setNo_cust(String no_cust) {
        this.no_cust = no_cust;
    }
}