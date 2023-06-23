package com.cv.integration.model;

import lombok.Data;
import jakarta.persistence.*;
import java.io.Serializable;

@Data
public class COAKey implements Serializable {
    private String coaCode;
    private String compCode;
}
