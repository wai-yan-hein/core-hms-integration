package com.cv.integration.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class COAOpening {
    private Date opDate;
    private String curCode;
    private Double crAmt;
    private Double drAmt;
    private String compCode;
    private Date createdDate;
    private String depCode;
    private String traderCode;
}
