package com.cv.integration.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class AccTrader implements java.io.Serializable {
    @NonNull
    private String traderCode;
    @NonNull
    private String userCode;
    @NonNull
    private String traderName;
    @NonNull
    private String appName;
    @NonNull
    private Boolean active;
    @NonNull
    private String discriminator;
    @NonNull
    private Integer macId;
    @NonNull
    private String compCode;

    public AccTrader() {
    }
}
