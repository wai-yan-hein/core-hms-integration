package com.cv.integration.model;

import lombok.Data;

import jakarta.persistence.*;

@Data
public class TraderKey {
    private String code;
    private String compCode;
}
