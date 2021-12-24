package com.cv.integration.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "currency")
public class Currency implements java.io.Serializable {
    @Id
    @Column(name = "cur_code")
    private String curCode;
    @Column(name = "acc_id")
    private String accCurCode;

    public Currency() {
    }
}
