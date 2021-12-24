package com.cv.integration.entity;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
    @Column(name = "trader_name")
    private String traderName;
    @Column(name = "discriminator")
    private String traderType;
    @Column(name = "active")
    private boolean active;
    @Column(name = "intg_upd_status")
    private String intgUpdStatus;

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
