package com.salesforecast.service;

import com.salesforecast.model.Product;
import com.salesforecast.repository.ProductRepository;
import com.salesforecast.repository.SalesRecordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepo;
    private final SalesRecordRepository salesRepo;

    public ProductService(ProductRepository productRepo, SalesRecordRepository salesRepo) {
        this.productRepo = productRepo;
        this.salesRepo = salesRepo;
    }

    public List<Product> findAll() { return productRepo.findAll(); }

    public Product create(Product p) {
        p.setId(null);
        return productRepo.save(p);
    }

    public Product update(Long id, Product p) {
        Product existing = productRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        existing.setName(p.getName());
        existing.setCategory(p.getCategory());
        existing.setPrice(p.getPrice());
        return productRepo.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        salesRepo.deleteByProductId(id); // remove its sales first (foreign key)
        productRepo.deleteById(id);
    }
}
