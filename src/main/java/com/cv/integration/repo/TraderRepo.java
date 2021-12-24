package com.cv.integration.repo;

import com.cv.integration.entity.Trader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TraderRepo extends JpaRepository<Trader, String> {
    @Query("select o from Trader o where o.active = true and o.intgUpdStatus is null")
    List<Trader> unUploadTrader();


}
