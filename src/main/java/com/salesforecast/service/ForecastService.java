package com.salesforecast.service;

import com.salesforecast.model.SalesRecord;
import com.salesforecast.repository.SalesRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.YearMonth;
import java.util.*;

/**
 * Forecasts monthly units and revenue.
 * Default: simple linear regression (least squares) in Java.
 * Optional: if forecast.ml.url is set, calls the Python ML service and falls back to Java if it fails.
 */
@Service
public class ForecastService {
    private final SalesRecordRepository salesRepo;

    @Value("${forecast.ml.url:}")
    private String mlUrl;

    public ForecastService(SalesRecordRepository salesRepo) {
        this.salesRepo = salesRepo;
    }

    public Map<String, Object> forecast(Long productId, int months) {
        months = Math.max(1, Math.min(months, 24));
        List<SalesRecord> records = productId == null ? salesRepo.findAll() : salesRepo.findByProductId(productId);

        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> points = new ArrayList<>();
        result.put("points", points);

        if (records.isEmpty()) {
            result.put("model", "No data");
            return result;
        }

        TreeMap<YearMonth, double[]> byMonth = new TreeMap<>();
        for (SalesRecord r : records) {
            double[] a = byMonth.computeIfAbsent(YearMonth.from(r.getSaleDate()), k -> new double[2]);
            a[0] += r.getQuantity();
            a[1] += r.getAmount().doubleValue();
        }

        YearMonth start = byMonth.firstKey();
        YearMonth end = byMonth.lastKey();
        List<YearMonth> axis = new ArrayList<>();
        for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) axis.add(ym);

        double[] units = new double[axis.size()];
        double[] revenue = new double[axis.size()];
        for (int i = 0; i < axis.size(); i++) {
            double[] a = byMonth.get(axis.get(i));
            if (a != null) { units[i] = a[0]; revenue[i] = a[1]; }
            points.add(point(axis.get(i), units[i], revenue[i], false));
        }

        boolean[] usedPython = {false};
        double[] fu = predict(units, months, usedPython);
        double[] fr = predict(revenue, months, usedPython);
        for (int i = 0; i < months; i++) {
            points.add(point(end.plusMonths(i + 1), fu[i], fr[i], true));
        }

        result.put("model", usedPython[0] ? "Python ML service (scikit-learn)" : "Linear Regression (Java)");
        result.put("monthsUsed", axis.size());
        return result;
    }

    private Map<String, Object> point(YearMonth ym, double units, double revenue, boolean forecast) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("month", ym.toString());
        p.put("units", Math.round(units));
        p.put("revenue", Math.round(revenue));
        p.put("forecast", forecast);
        return p;
    }

    private double[] predict(double[] y, int months, boolean[] usedPython) {
        if (mlUrl != null && !mlUrl.isBlank()) {
            double[] viaPy = callPython(y, months);
            if (viaPy != null) { usedPython[0] = true; return viaPy; }
        }
        return linear(y, months);
    }

    static double[] linear(double[] y, int months) {
        int n = y.length;
        double[] out = new double[months];
        if (n < 2) {
            Arrays.fill(out, n == 1 ? y[0] : 0);
            return out;
        }
        double sx = 0, sy = 0, sxy = 0, sxx = 0;
        for (int i = 0; i < n; i++) {
            sx += i; sy += y[i]; sxy += i * y[i]; sxx += (double) i * i;
        }
        double denom = n * sxx - sx * sx;
        double slope = denom == 0 ? 0 : (n * sxy - sx * sy) / denom;
        double intercept = (sy - slope * sx) / n;
        for (int k = 0; k < months; k++) {
            out[k] = Math.max(0, intercept + slope * (n + k));
        }
        return out;
    }

    private double[] callPython(double[] values, int months) {
        try {
            RestTemplate rt = new RestTemplate();
            Map<String, Object> body = new HashMap<>();
            body.put("values", values);
            body.put("months", months);
            Map<?, ?> resp = rt.postForObject(mlUrl, body, Map.class);
            if (resp == null) return null;
            List<?> preds = (List<?>) resp.get("predictions");
            if (preds == null || preds.size() < months) return null;
            double[] out = new double[months];
            for (int i = 0; i < months; i++) out[i] = ((Number) preds.get(i)).doubleValue();
            return out;
        } catch (Exception e) {
            System.out.println("Python ML service unavailable, using Java regression: " + e.getMessage());
            return null;
        }
    }
}
