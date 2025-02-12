package com.restful.tc.model;

import jakarta.persistence.*;

@Entity
@Table(name = "share")
public class Share {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "no_share")
    private String noShare;

    @Column(name = "descr")
    private String descr;

    public String getNoShare() {
        return noShare;
    }

    public void setNoShare(String noShare) {
        this.noShare = noShare;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }


}
