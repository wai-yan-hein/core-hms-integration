package com.cv.integration.repo;

import com.cv.integration.entity.SysProperty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysPropertyRepo extends JpaRepository<SysProperty, String> {
}
