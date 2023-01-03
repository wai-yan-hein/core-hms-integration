package com.cv.integration.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Response {
    private String message;
    private String vouNo;
    private String tranSource;
    private String compCode;
}
