package com.cv.integration.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
public class COAKey implements Serializable {
    private String coaCode;
    private String compCode;
}
