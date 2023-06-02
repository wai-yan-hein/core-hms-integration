package com.cv.integration.repo;

import com.cv.integration.entity.TraderOpening;
import com.cv.integration.entity.TraderOpeningKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface TraderOpeningRepo extends JpaRepository<TraderOpening, TraderOpeningKey> {
    @Query("select o from TraderOpening o where o.intgUpdStatus is null and date(o.key.opDate) = :vou_date")
    List<TraderOpening> unUploadVoucher(@Param("vou_date") Date syncDate);

    @Transactional
    @Modifying
    @Query("update TraderOpening o set o.intgUpdStatus = :status where o.key.trader.traderCode = :traderCode")
    void updateOpening(@Param("traderCode") String traderCode, @Param("status") String status);
}
