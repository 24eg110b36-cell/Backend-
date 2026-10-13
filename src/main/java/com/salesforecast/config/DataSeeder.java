package com.salesforecast.config;

import com.salesforecast.model.Product;
import com.salesforecast.model.SalesRecord;
import com.salesforecast.repository.ProductRepository;
import com.salesforecast.repository.SalesRecordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Inserts sample laptops/mobiles and 24 months of sales the first time the app runs. */
@Component
public class DataSeeder implements CommandLineRunner {
    private final ProductRepository products;
    private final SalesRecordRepository sales;

    public DataSeeder(ProductRepository products, SalesRecordRepository sales) {
        this.products = products;
        this.sales = sales;
    }

    @Override
    public void run(String... args) {
        if (products.count() > 0) return;

        List<Product> list = products.saveAll(List.of(
                new Product("Dell Inspiron Laptop", "Laptop", new BigDecimal("55000")),
                new Product("HP Pavilion Laptop", "Laptop", new BigDecimal("62000")),
                new Product("MacBook Air M2", "Laptop", new BigDecimal("105000")),
                new Product("Samsung Galaxy S23", "Mobile", new BigDecimal("74999")),
                new Product("iPhone 14", "Mobile", new BigDecimal("79900")),
                new Product("OnePlus 11", "Mobile", new BigDecimal("56999"))
        ));

        Random rnd = new Random(42);
        int[] base = {18, 15, 8, 25, 20, 22};
        double[] growth = {0.02, 0.015, 0.03, 0.025, 0.01, 0.02};
        List<SalesRecord> records = new ArrayList<>();
        LocalDate now = LocalDate.now().withDayOfMonth(1);

        for (int m = 23; m >= 0; m--) {
            LocalDate month = now.minusMonths(m);
            int idx = 23 - m;
            double seasonal = (month.getMonthValue() >= 10 || month.getMonthValue() == 1) ? 1.25 : 1.0; // festive season
            for (int p = 0; p < list.size(); p++) {
                int qty = (int) Math.max(1, Math.round(base[p] * (1 + growth[p] * idx) * seasonal + rnd.nextInt(5) - 2));
                Product prod = list.get(p);
                BigDecimal amount = prod.getPrice().multiply(BigDecimal.valueOf(qty));
                records.add(new SalesRecord(prod, month.withDayOfMonth(15), qty, amount));
            }
        }
        sales.saveAll(records);
        System.out.println("Sample data inserted: " + list.size() + " products, " + records.size() + " sales records.");
    }
}
