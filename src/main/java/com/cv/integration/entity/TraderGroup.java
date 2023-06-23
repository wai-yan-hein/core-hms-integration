package com.cv.integration.entity;

import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "customer_group")
public class TraderGroup {
    @Id
    @Column(name = "group_id")
    private String groupId;
    @Column(name = "group_name")
    private String groupName;
    @Column(name = "account_id")
    private String accountId;
    @Column(name = "dept_id")
    private String deptCode;
}
