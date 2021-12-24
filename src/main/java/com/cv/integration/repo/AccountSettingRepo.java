package com.cv.integration.repo;

import com.cv.integration.entity.AccountSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountSettingRepo extends JpaRepository<AccountSetting, String> {
}
