package com.salesforecast.service;

import com.salesforecast.dto.SalesRequest;
import com.salesforecast.model.Product;
import com.salesforecast.model.SalesRecord;
import com.salesforecast.repository.ProductRepository;
import com.salesforecast.repository.SalesRecordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SalesService {
    private final SalesRecordRepository salesRepo;
    private final ProductRepository productRepo;

    public SalesService(SalesRecordRepository salesRepo, ProductRepository productRepo) {
        this.salesRepo = salesRepo;
        this.productRepo = productRepo;
    }

    public List<SalesRecord> findAll() {
        return salesRepo.findAllByOrderBySaleDateDescIdDesc();
    }

    @Transactional
    public SalesRecord save(Long id, SalesRequest req) {
        Product product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        SalesRecord record = (id == null) ? new SalesRecord()
                : salesRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale not found"));
        record.setProduct(product);
        record.setSaleDate(req.getSaleDate());
        record.setQuantity(req.getQuantity());
        BigDecimal amount = req.getAmount() != null && req.getAmount().signum() > 0
                ? req.getAmount()
                : product.getPrice().multiply(BigDecimal.valueOf(req.getQuantity()));
        record.setAmount(amount);
        return salesRepo.save(record);
    }

    public void delete(Long id) {
        if (!salesRepo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale not found");
        salesRepo.deleteById(id);
    }
}
