package com.cv.integration.model;

import com.cv.integration.entity.OpeningKey;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class COAOpening {
    private OpeningKey key;
    private Date opDate;
    private String curCode;
    private Double crAmt;
    private Double drAmt;
    private Date createdDate;
    private String deptCode;
    private String traderCode;
}
