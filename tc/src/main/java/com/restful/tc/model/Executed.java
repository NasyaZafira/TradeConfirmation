package com.restful.tc.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "executed")
public class Executed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "no_done")
    private String noDone;

    @Column(name = "board")
    private String board;

    @Column(name = "no_share")
    private String noShare;

    @Column(name = "vol_done")
    private BigDecimal volDone;

    @Column(name = "prc_done")
    private BigDecimal prcDone;

    public Executed() {

    }

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

    public String getNoShare() {
        return noShare;
    }

    public void setNoShare(String noShare) {
        this.noShare = noShare;
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

}
