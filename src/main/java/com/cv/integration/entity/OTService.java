package com.cv.integration.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "ot_service")
public class OTService {
    @Id
    @Column(name = "service_id")
    private Integer serviceId;
    @Column(name = "service_name")
    private String serviceName;
    @ManyToOne
    @JoinColumn(name = "group_id")
    private OTGroup otGroup;

    public OTService() {
    }
}
