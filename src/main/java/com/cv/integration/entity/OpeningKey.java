package com.cv.integration.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
public class OpeningKey {
    private String opId;
    private String compCode;
}
