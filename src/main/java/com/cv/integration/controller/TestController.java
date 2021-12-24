package com.cv.integration.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TestController {
    @RequestMapping("/admin")
    public String test() {
        return "Hello World";
    }
}
