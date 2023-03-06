package com.cv.integration.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TestController {
    @PostMapping("/admin")
    public String test() {
        return "Hello World";
    }
}
