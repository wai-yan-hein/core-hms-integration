package com.cv.integration.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ChartOfAccount {

    private COAKey key;
    private String coaNameEng;
    private boolean active;
    private LocalDateTime createdDate;
    private String createdBy;
    private String coaParent;
    private String coaOption;
    private Integer coaLevel;
    private Integer macId;
    private String migCode;
    private boolean deleted;
    private boolean credit;
    private boolean marked;

}
