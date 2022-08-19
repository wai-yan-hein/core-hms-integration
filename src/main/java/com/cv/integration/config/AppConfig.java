package com.cv.integration.config;

import com.cv.integration.entity.SysProperty;
import com.cv.integration.repo.SysPropertyRepo;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@AllArgsConstructor
public class AppConfig {
    @Autowired
    private SysPropertyRepo sysPropertyRepo;

    @Bean
    public String dcDepositId() {
        Optional<SysProperty> sys = sysPropertyRepo.findById("system.dc.deposite.id");
        return sys.isPresent() ? sys.get().getPropValue() : "0";
    }

    @Bean
    public String dcDiscountId() {
        Optional<SysProperty> sys = sysPropertyRepo.findById("system.dc.disc.id");
        return sys.isPresent() ? sys.get().getPropValue() : "0";
    }

    @Bean
    public String dcPaidId() {
        Optional<SysProperty> sys = sysPropertyRepo.findById("system.dc.paid.id");
        return sys.isPresent() ? sys.get().getPropValue() : "0";
    }

    @Bean
    public String dcRefundId() {
        Optional<SysProperty> sys = sysPropertyRepo.findById("system.dc.refund.id");
        return sys.isPresent() ? sys.get().getPropValue() : "0";
    }

    @Bean
    public String otDepositId() {
        Optional<SysProperty> sys = sysPropertyRepo.findById("system.ot.deposite.id");
        return sys.isPresent() ? sys.get().getPropValue() : "0";
    }

    @Bean
    public String otDiscountId() {
        Optional<SysProperty> sys = sysPropertyRepo.findById("system.ot.disc.id");
        return sys.isPresent() ? sys.get().getPropValue() : "0";
    }

    @Bean
    public String otPaidId() {
        Optional<SysProperty> sys = sysPropertyRepo.findById("system.ot.paid.id");
        return sys.isPresent() ? sys.get().getPropValue() : "0";
    }

    @Bean
    public String otRefundId() {
        Optional<SysProperty> sys = sysPropertyRepo.findById("system.ot.refund.id");
        return sys.isPresent() ? sys.get().getPropValue() : "0";
    }

    @Bean
    public String packageId() {
        Optional<SysProperty> sys = sysPropertyRepo.findById("system.dc.pkggain.id");
        return sys.isPresent() ? sys.get().getPropValue() : "0";
    }

}
