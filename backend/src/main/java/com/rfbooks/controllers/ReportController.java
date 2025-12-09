package com.rfbooks.controllers;

import com.rfbooks.dtos.FinancialReportDto;
import com.rfbooks.dtos.ProfitLossReportDto;
import com.rfbooks.services.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/financial")
    public ResponseEntity<FinancialReportDto> getFinancialReport(
            @RequestParam(required = false) String resortAlias,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        FinancialReportDto report = reportService.generateFinancialReport(period, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/profit-loss")
    public ResponseEntity<ProfitLossReportDto> getProfitLossReport(
            @RequestParam(required = false) String resortAlias,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        ProfitLossReportDto report = reportService.generateProfitLossReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
}
