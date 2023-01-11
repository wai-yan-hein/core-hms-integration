package com.cv.integration.model;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;

@Data
public class Gl implements java.io.Serializable {

    private GlKey key;
    private Date glDate;
    private String description;
    private String srcAccCode;
    private String accCode;
    private String curCode;
    private Double drAmt;
    private Double crAmt;
    private String reference;
    private String deptCode;
    private String vouNo;
    private String traderCode;
    private Date createdDate;
    private String modifyBy;
    private String createdBy;
    private String tranSource;
    private String glVouNo;
    private String remark;
    private Integer macId;
    private String refNo;
    private boolean cash = false;
    private boolean deleted;
    public Gl() {
    }
}
