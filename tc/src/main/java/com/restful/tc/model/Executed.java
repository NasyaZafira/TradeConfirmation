package com.restful.tc.model;

import jakarta.persistence.*;

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
    private Integer volDone;

    @Column(name = "prc_done")
    private Integer prcDone;

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

    public Integer getVolDone() {
        return volDone;
    }

    public void setVolDone(Integer volDone) {
        this.volDone = volDone;
    }

    public Integer getPrcDone() {
        return prcDone;
    }

    public void setPrcDone(Integer prcDone) {
        this.prcDone = prcDone;
    }

}
