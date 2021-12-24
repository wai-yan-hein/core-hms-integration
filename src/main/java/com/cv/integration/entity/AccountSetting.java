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
@Table(name = "acc_setting")
public class AccountSetting {
    @Id
    private String type;
    @Column(name = "source_acc")
    private String sourceAcc;
    @Column(name = "pay_acc")
    private String payAcc;
    @Column(name = "dis_acc")
    private String discountAcc;
    @Column(name = "bal_acc")
    private String balanceAcc;
    @Column(name = "tax_acc")
    private String taxAcc;
    @Column(name = "dep_code")
    private String deptCode;
    @Column(name = "source_acc_1")
    private String ipdSource;
    @Column(name = "pay_acc_out")
    private String payAccOut;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AccountSetting setting = (AccountSetting) o;
        return Objects.equals(type, setting.type);
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
