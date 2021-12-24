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
@Table(name = "sys_prop")
public class SysProperty implements java.io.Serializable {
    @Id
    @Column(name = "sys_prop_desp")
    private String propKey;
    @Column(name = "sys_prop_value")
    private String propValue;

    public SysProperty() {
    }
}
