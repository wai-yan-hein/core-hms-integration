package com.cv.integration.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "inp_service")
public class DCService implements java.io.Serializable {
    @Id
    @Column(name = "service_id")
    private Integer serviceId;
    @Column(name = "service_name")
    private String serviceName;
    @ManyToOne
    @JoinColumn(name = "cat_id")
    private DCGroup dcGroup;

    public DCService() {
    }
}
