package com.restful.tc.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "executed")
public class Executed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "no_done")
    private String noDone;

    @Column(name = "board")
    private String board;

    @Column(name = "no_cust")
    private String noCust;

    @Column(name = "dt_inv")
    private LocalDate executionDate;

    @Column(name = "vol_done")
    private BigDecimal volDone;

    @Column(name = "prc_done")
    private BigDecimal prcDone;

    @Column(name = "no_inv")
    private String noInv;

    public Executed(Share share, String bors, String noInv, BigDecimal prcDone, BigDecimal volDone, LocalDate executionDate, String noCust, String board, String noDone) {
        this.share = share;
        this.bors = bors;
        this.noInv = noInv;
        this.prcDone = prcDone;
        this.volDone = volDone;
        this.executionDate = executionDate;
        this.noCust = noCust;
        this.board = board;
        this.noDone = noDone;
    }

    @Column(name = "bors")
    private String bors;

    @ManyToOne
    @JoinColumn(name = "no_share", referencedColumnName = "no_share")
    private Share share;

    // Default no-argument constructor (required by Hibernate)
    public Executed() {
    }



    // Getters and setters
    public String getNoDone() {
        return noDone;
    }

    public void setNoDone(String noDone) {
        this.noDone = noDone;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public String getNoCust() {
        return noCust;
    }

    public void setNoCust(String noCust) {
        this.noCust = noCust;
    }

    public LocalDate getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDate executionDate) {
        this.executionDate = executionDate;
    }

    public BigDecimal getVolDone() {
        return volDone;
    }

    public void setVolDone(BigDecimal volDone) {
        this.volDone = volDone;
    }

    public BigDecimal getPrcDone() {
        return prcDone;
    }

    public void setPrcDone(BigDecimal prcDone) {
        this.prcDone = prcDone;
    }

    public String getNoInv() {
        return noInv;
    }

    public void setNoInv(String noInv) {
        this.noInv = noInv;
    }

    public String getBors() {
        return bors;
    }

    public void setBors(String bors) {
        this.bors = bors;
    }

    public Share getShare() {
        return share;
    }

    public void setShare(Share share) {
        this.share = share;
    }
}
