package com.cv.integration.repo;

import com.cv.integration.entity.OTService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OTServiceRepo extends JpaRepository<OTService, Integer> {
    @Query("select o from OTService o where o.otGroup.groupId = :groupId and o.serviceId=:serviceId")
    List<OTService> searchGroup(@Param("groupId") Integer groupId, @Param("serviceId") Integer serviceId);
}
