package com.cv.integration.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import jakarta.persistence.*;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Trader trader = (Trader) o;
        return Objects.equals(traderCode, trader.traderCode);
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
