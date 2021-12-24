package com.cv.integration.config;

import com.cv.integration.entity.AccountSetting;
import com.cv.integration.repo.AccountSettingRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
public class AccSettingConfig {
    @Autowired
    private final AccountSettingRepo accountSettingRepo;

    public AccSettingConfig(AccountSettingRepo accountSettingRepo) {
        this.accountSettingRepo = accountSettingRepo;
    }

    @Bean
    public Map<String, AccountSetting> hmAccSetting() {
        Map<String, AccountSetting> hmAccSetting = new HashMap<>();
        List<AccountSetting> list = accountSettingRepo.findAll();
        if (!list.isEmpty()) {
            for (AccountSetting setting : list) {
                hmAccSetting.put(setting.getType(), setting);
            }
            log.info("Account Setting configured.");
        } else {
            throw new IllegalStateException("Account Setting need to configure.");
        }
        return hmAccSetting;
    }
}
