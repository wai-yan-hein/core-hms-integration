package com.cv.integration.mongo.model;

import lombok.Data;

@Data
public class Region {
    private String id;
    private String cityName;
    private String parent;

    public Region() {
    }

    public Region(String cityName) {
        this.cityName = cityName;
    }
}
