package com.cv.integration.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Embeddable
public class TraderOpeningKey implements java.io.Serializable {
    @Column(name = "op_date")
    @Temporal(TemporalType.DATE)
    private Date opDate;
    @ManyToOne
    @JoinColumn(name = "trader_id")
    private Trader trader;
    @Column(name = "currency")
    private String curCode;
}
