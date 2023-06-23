package com.cv.integration.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "opd_service")
public class OPDService implements java.io.Serializable {
    @Id
    @Column(name = "service_id")
    private Integer serviceId;
    @Column(name = "service_name")
    private String serviceName;
    @Column(name = "is_percent")
    private boolean percent;
    @ManyToOne
    @JoinColumn(name = "cat_id")
    private OPDCategory category;

    public OPDService() {
    }
}
