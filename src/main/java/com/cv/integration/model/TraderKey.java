package com.cv.integration.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
public class TraderKey {
    private String code;
    private String compCode;
}
