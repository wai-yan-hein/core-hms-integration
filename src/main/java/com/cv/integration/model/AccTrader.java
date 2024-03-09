package com.cv.integration.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class AccTrader implements java.io.Serializable {
    private TraderKey key;
    private String userCode;
    private String traderName;
    private String appName;
    private Boolean active;
    private String traderType;
    private String account;
    private String accountName;
    private String accountParent;
    private String groupCode;
    private Integer macId;
    private String createdBy;
    private boolean deleted;

    public AccTrader() {
    }
}
