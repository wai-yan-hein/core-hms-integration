package com.cv.integration.entity;

import lombok.Data;
import jakarta.persistence.*;
@Data
@Entity
@Table(name = "trader_op")
public class TraderOpening {
    @EmbeddedId
    private TraderOpeningKey key;
    @Column(name = "op_amount")
    private Double amount;
    @Column(name = "intg_upd_status")
    private String intgUpdStatus;
}
