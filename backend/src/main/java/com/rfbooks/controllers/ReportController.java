package com.rfbooks.controllers;

import com.rfbooks.dtos.CategoryBreakdown;
import com.rfbooks.dtos.FinancialSummary;
import com.rfbooks.dtos.MonthlyTrend;
import com.rfbooks.services.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ResponseEntity<FinancialSummary> getSummary(
            @RequestParam(required = false) String resortAlias,
            @RequestParam(defaultValue = "MONTH") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate reference = date != null ? date : LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;
        String periodLabel;

        switch (period.toUpperCase()) {
            case "QUARTER":
                int qMonth = ((reference.getMonthValue() - 1) / 3) * 3 + 1;
                startDate = LocalDate.of(reference.getYear(), qMonth, 1);
                endDate = startDate.plusMonths(3).minusDays(1);
                periodLabel = "Q" + ((qMonth - 1) / 3 + 1) + " " + reference.getYear();
                break;
            case "YEAR":
                startDate = LocalDate.of(reference.getYear(), 1, 1);
                endDate = LocalDate.of(reference.getYear(), 12, 31);
                periodLabel = String.valueOf(reference.getYear());
                break;
            case "CUSTOM":
                // For custom, expect startDate and endDate as separate params—fall through to MONTH if not provided
            default: // MONTH
                YearMonth ym = YearMonth.from(reference);
                startDate = ym.atDay(1);
                endDate = ym.atEndOfMonth();
                periodLabel = ym.toString();
                break;
        }

        return ResponseEntity.ok(reportService.getSummary(startDate, endDate, periodLabel));
    }

    @GetMapping("/income-by-category")
    public ResponseEntity<List<CategoryBreakdown>> getIncomeByCategory(
            @RequestParam(required = false) String resortAlias,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getIncomeByCategory(startDate, endDate));
    }

    @GetMapping("/expenses-by-category")
    public ResponseEntity<List<CategoryBreakdown>> getExpensesByCategory(
            @RequestParam(required = false) String resortAlias,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getExpensesByCategory(startDate, endDate));
    }

    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyTrend>> getMonthlyTrend(
            @RequestParam(required = false) String resortAlias,
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(reportService.getMonthlyTrend(months));
    }
}
