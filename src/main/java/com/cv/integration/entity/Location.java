package com.cv.integration.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "location")
public class Location implements java.io.Serializable {
    @Id
    @Column(name = "location_id")
    private Integer locId;
    @Column(name = "location_name")
    private String locName;
    @Column(name = "account_code")
    private String accCode;
    @Column(name = "acc_dept_code")
    private String deptCode;
    @Column(name = "trader_code")
    private String traderCode;
    @Column(name = "pur_account_code")
    private String purAccount;

    public Location() {
    }
}
