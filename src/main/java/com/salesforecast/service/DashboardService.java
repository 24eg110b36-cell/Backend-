package com.salesforecast.service;

import com.salesforecast.model.SalesRecord;
import com.salesforecast.repository.ProductRepository;
import com.salesforecast.repository.SalesRecordRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;

@Service
public class DashboardService {
    private final SalesRecordRepository salesRepo;
    private final ProductRepository productRepo;

    public DashboardService(SalesRecordRepository salesRepo, ProductRepository productRepo) {
        this.salesRepo = salesRepo;
        this.productRepo = productRepo;
    }

    public Map<String, Object> summary() {
        List<SalesRecord> all = salesRepo.findAll();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalUnits = 0;
        Map<String, double[]> byProduct = new LinkedHashMap<>();   // [revenue, units]
        Map<String, double[]> byCategory = new LinkedHashMap<>();
        TreeMap<YearMonth, double[]> byMonth = new TreeMap<>();

        for (SalesRecord s : all) {
            totalRevenue = totalRevenue.add(s.getAmount());
            totalUnits += s.getQuantity();
            String cat = s.getProduct().getCategory() == null ? "Other" : s.getProduct().getCategory();
            add(byProduct, s.getProduct().getName(), s);
            add(byCategory, cat, s);
            double[] a = byMonth.computeIfAbsent(YearMonth.from(s.getSaleDate()), k -> new double[2]);
            a[0] += s.getAmount().doubleValue();
            a[1] += s.getQuantity();
        }

        List<Map<String, Object>> productRows = rows(byProduct);
        productRows.sort((x, y) -> Double.compare((double) y.get("revenue"), (double) x.get("revenue")));

        List<Map<String, Object>> monthly = new ArrayList<>();
        byMonth.forEach((ym, v) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("month", ym.toString());
            m.put("revenue", v[0]);
            m.put("units", (long) v[1]);
            monthly.add(m);
        });

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("totalRevenue", totalRevenue);
        out.put("totalUnits", totalUnits);
        out.put("totalSales", all.size());
        out.put("productCount", productRepo.count());
        out.put("topProduct", productRows.isEmpty() ? "-" : productRows.get(0).get("name"));
        out.put("revenueByProduct", productRows);
        out.put("revenueByCategory", rows(byCategory));
        out.put("monthlyTrend", monthly);
        return out;
    }

    private void add(Map<String, double[]> map, String key, SalesRecord s) {
        double[] a = map.computeIfAbsent(key, k -> new double[2]);
        a[0] += s.getAmount().doubleValue();
        a[1] += s.getQuantity();
    }

    private List<Map<String, Object>> rows(Map<String, double[]> map) {
        List<Map<String, Object>> list = new ArrayList<>();
        map.forEach((k, v) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", k);
            m.put("revenue", v[0]);
            m.put("units", (long) v[1]);
            list.add(m);
        });
        return list;
    }
}
