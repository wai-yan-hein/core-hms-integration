package com.cv.integration.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Gl {

    private GlKey key;
    private LocalDateTime glDate;
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
    private LocalDateTime createdDate;
    private String modifyBy;
    private String createdBy;
    private String tranSource;
    private String glVouNo;
    private String remark;
    private Integer macId;
    private String refNo;
    private boolean cash = false;
    private boolean deleted;
    private String patientNo;
    private String doctorId;
    private String serviceId;

    public Gl() {
    }
}
