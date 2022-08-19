package com.cv.integration.model;

import lombok.Data;
import lombok.NonNull;

import java.util.Date;

@Data
public class Gl implements java.io.Serializable {

    @NonNull
    private String glCode;
    @NonNull
    private Date glDate;
    private String description;
    private String srcAccCode;
    private String accCode;
    @NonNull
    private String curCode;
    private Double drAmt;
    private Double crAmt;
    private String reference;
    @NonNull
    private String deptCode;
    private String traderCode;
    @NonNull
    private String compCode;
    private Date createdDate;
    private String createdBy;
    @NonNull
    private String tranSource;
    private String refNo;
    private boolean deleted;
    @NonNull
    private Integer macId;
    private boolean cash = false;
    private String coaParent;
    private String migId;
    private String migName;
    private String traderGroup;

    public Gl() {
    }
}
