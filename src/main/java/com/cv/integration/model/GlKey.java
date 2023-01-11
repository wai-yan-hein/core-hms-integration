package com.cv.integration.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class GlKey implements Serializable {
    private String glCode;
    private String compCode;
    private Integer deptId;
}
