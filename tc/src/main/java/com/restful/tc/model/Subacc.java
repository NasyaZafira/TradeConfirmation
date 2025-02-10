package com.restful.tc.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "subacc")
public class Subacc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "no_cust")
    private String noCust;

    @Column(name = "name")
    private String name;

    @Column(name = "add1")
    private String add1;

    @Column(name = "add2")
    private String add2;

    @Column(name = "city")
    private String city;

    @Column(name = "zip")
    private String zip;

    @Column(name = "country")
    private String country;

    @Column(name = "phone1")
    private String phone1;

    @Column(name = "email")
    private String email;

    @Column(name = "bank")
    private String bank;

    @Column(name = "account")
    private String account;

    @Column(name = "no_staff")
    private String no_staff;

    @Column(name = "investor_no")
    private String investor_no;

    @Column(name = "no_ksei")
    private String no_ksei;

    @Column(name = "poem_com")
    private BigDecimal poem_com;

    @Column(name = "no_sub")
    private String no_sub;

    public Subacc(){

    }

    public String getNoCust() {
        return noCust;
    }

    public void setNoCust(String noCust) {
        this.noCust = noCust;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdd1() {
        return add1;
    }

    public void setAdd1(String add1) {
        this.add1 = add1;
    }

    public String getAdd2() {
        return add2;
    }

    public void setAdd2(String add2) {
        this.add2 = add2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNo_staff() {
        return no_staff;
    }

    public void setNo_staff(String no_staff) {
        this.no_staff = no_staff;
    }

    public String getInvestor_no() {
        return investor_no;
    }

    public void setInvestor_no(String investor_no) {
        this.investor_no = investor_no;
    }

    public String getNo_ksei() {
        return no_ksei;
    }

    public void setNo_ksei(String no_ksei) {
        this.no_ksei = no_ksei;
    }

    public BigDecimal getPoem_com() {
        return poem_com;
    }

    public void setPoem_com(BigDecimal poem_com) {
        this.poem_com = poem_com;
    }

    public String getNo_sub() {
        return no_sub;
    }

    public void setNo_sub(String no_sub) {
        this.no_sub = no_sub;
    }


}
