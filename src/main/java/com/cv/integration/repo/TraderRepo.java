package com.cv.integration.repo;

import com.cv.integration.entity.Trader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TraderRepo extends JpaRepository<Trader, String> {
    @Query("select o from Trader o where o.active = true and o.intgUpdStatus is null")
    List<Trader> unUploadTrader();

    @Transactional
    @Modifying
    @Query("update Trader o set o.intgUpdStatus = :status where o.traderCode = :traderCode")
    void updateTrader(@Param("traderCode") String traderCode, @Param("status") String status);


}
