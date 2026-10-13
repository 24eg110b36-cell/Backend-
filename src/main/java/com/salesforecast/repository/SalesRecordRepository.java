package com.salesforecast.repository;

import com.salesforecast.model.SalesRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalesRecordRepository extends JpaRepository<SalesRecord, Long> {
    List<SalesRecord> findAllByOrderBySaleDateDescIdDesc();
    List<SalesRecord> findByProductId(Long productId);
    void deleteByProductId(Long productId);
}
