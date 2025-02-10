package com.restful.tc.model;

import jakarta.persistence.*;

@Entity
@Table(name = "subacc")
public class Subacc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "no_cust")
    private String noCust;

    @Column(name = "name")
    private String name;
}
