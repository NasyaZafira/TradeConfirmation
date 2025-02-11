package com.restful.tc.model;

import jakarta.persistence.*;

@Entity
@Table(name = "staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "no_staff")
    private String noStaff;

    @Column(name = "name")
    private String name;

    public String getNoStaff() {
        return noStaff;
    }

    public void setNoStaff(String noStaff) {
        this.noStaff = noStaff;
    }

    public String getNameStaff() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
