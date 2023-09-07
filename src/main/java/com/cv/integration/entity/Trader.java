package com.cv.integration.entity;

import lombok.*;
import org.hibernate.Hibernate;

import jakarta.persistence.*;

import java.util.Objects;

@Data
@Entity
@Table(name = "trader")
public class Trader {
    @Id
    @Column(name = "trader_id")
    private String traderCode;
    @Column(name = "stu_no")
    private String userCode;
    @Column(name = "trader_name")
    private String traderName;
    @Column(name = "discriminator")
    private String traderType;
    @Column(name = "active")
    private boolean active;
    @Column(name = "intg_upd_status")
    private String intgUpdStatus;
    @ManyToOne
    @JoinColumn(name = "group_id")
    private TraderGroup traderGroup;
    @Column(name = "account_code")
    private String account;
}
