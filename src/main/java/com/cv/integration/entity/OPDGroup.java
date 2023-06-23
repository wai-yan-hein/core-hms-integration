package com.cv.integration.entity;

import lombok.*;
import org.hibernate.Hibernate;

import jakarta.persistence.*;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "opd_group")
public class OPDGroup implements java.io.Serializable {
    @Id
    @Column(name = "group_id")
    private Integer groupId;
    @Column(name = "group_name")
    private String groupName;
    @Column(name = "account")
    private String account;
    @Column(name = "dept_code")
    private String deptCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        OPDGroup opdGroup = (OPDGroup) o;
        return groupId != null && Objects.equals(groupId, opdGroup.groupId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
