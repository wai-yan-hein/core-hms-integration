package com.cv.integration.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ChartOfAccount {

    private String coaCode;
    private String coaNameEng;
    private boolean active;
    private Date createdDate;
    private String createdBy;
    private String coaParent;
    private String option;
    private String compCode;
    private Integer coaLevel;
    private Integer macId;
    private String migCode;
}
