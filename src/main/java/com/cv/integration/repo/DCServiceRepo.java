package com.cv.integration.repo;

import com.cv.integration.entity.DCService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DCServiceRepo extends JpaRepository<DCService, Integer> {
    @Query("select o from DCService o where o.dcGroup.groupId = :groupId and o.serviceId=:serviceId")
    List<DCService> searchGroup(@Param("groupId") Integer groupId, @Param("serviceId") Integer serviceId);
}
