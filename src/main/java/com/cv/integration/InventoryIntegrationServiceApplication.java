package com.cv.integration;

import com.cv.integration.common.Tray;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.awt.*;

@SpringBootApplication
@EnableScheduling
public class InventoryIntegrationServiceApplication {

    public static void main(String[] args) throws AWTException {
        new Tray().startup();
        SpringApplication.run(InventoryIntegrationServiceApplication.class, args);
    }

}
