package com.fintax.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintax.dto.calculator.*;
import com.fintax.entity.CalculatorHistory;
import com.fintax.entity.User;
import com.fintax.repository.CalculatorHistoryRepository;
import com.fintax.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalculatorService {

    private static final String DISCLAIMER =
        "This is an estimate for educational purposes only. Verify with official government sources or a qualified professional.";

    private final CalculatorHistoryRepository calculatorHistoryRepository;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;

    // ---------- Income Tax (simplified new regime slabs, FY illustrative) ----------
    public CalculatorResultResponse calculateIncomeTax(IncomeTaxRequest req) {
        double taxableIncome = "OLD".equalsIgnoreCase(req.getRegime())
                ? Math.max(0, req.getAnnualIncome() - req.getTotalDeductions())
                : req.getAnnualIncome();

        double tax = "OLD".equalsIgnoreCase(req.getRegime())
                ? computeOldRegimeTax(taxableIncome)
                : computeNewRegimeTax(taxableIncome);

        double cess = tax * 0.04; // 4% health & education cess
        double totalTax = tax + cess;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("regime", req.getRegime().toUpperCase());
        result.put("taxableIncome", round(taxableIncome));
        result.put("taxBeforeCess", round(tax));
        result.put("cess", round(cess));
        result.put("totalTaxPayable", round(totalTax));

        CalculatorResultResponse response = new CalculatorResultResponse("INCOME_TAX", result, DISCLAIMER);
        saveHistory(CalculatorHistory.CalculatorType.INCOME_TAX, req, result);
        return response;
    }

    private double computeNewRegimeTax(double income) {
        double[] slabLimits = {300000, 600000, 900000, 1200000, 1500000};
        double[] rates = {0.05, 0.10, 0.15, 0.20, 0.30};
        return computeSlabTax(income, slabLimits, rates);
    }

    private double computeOldRegimeTax(double income) {
        double[] slabLimits = {250000, 500000, 1000000};
        double[] rates = {0.05, 0.20, 0.30};
        return computeSlabTax(income, slabLimits, rates);
    }

    private double computeSlabTax(double income, double[] slabLimits, double[] rates) {
        double tax = 0;
        double previousLimit = slabLimits[0]; // first slab is 0% up to slabLimits[0]
        for (int i = 0; i < rates.length; i++) {
            double upperLimit = (i + 1 < slabLimits.length) ? slabLimits[i + 1] : Double.MAX_VALUE;
            if (income > previousLimit) {
                double taxableInSlab = Math.min(income, upperLimit) - previousLimit;
                tax += taxableInSlab * rates[i];
            }
            previousLimit = upperLimit;
        }
        return Math.max(tax, 0);
    }

    // ---------- EMI ----------
    public CalculatorResultResponse calculateEmi(EmiRequest req) {
        double monthlyRate = (req.getAnnualRatePercent() / 12) / 100;
        int n = req.getTenureMonths();
        double p = req.getPrincipal();

        double emi = monthlyRate == 0
                ? p / n
                : (p * monthlyRate * Math.pow(1 + monthlyRate, n)) / (Math.pow(1 + monthlyRate, n) - 1);

        double totalPayment = emi * n;
        double totalInterest = totalPayment - p;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("monthlyEmi", round(emi));
        result.put("totalPayment", round(totalPayment));
        result.put("totalInterest", round(totalInterest));

        CalculatorResultResponse response = new CalculatorResultResponse("EMI", result, DISCLAIMER);
        saveHistory(CalculatorHistory.CalculatorType.EMI, req, result);
        return response;
    }

    // ---------- Simple Interest ----------
    public CalculatorResultResponse calculateSimpleInterest(InterestRequest req) {
        double interest = (req.getPrincipal() * req.getRatePercent() * req.getYears()) / 100;
        double maturity = req.getPrincipal() + interest;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("interestEarned", round(interest));
        result.put("maturityAmount", round(maturity));

        CalculatorResultResponse response = new CalculatorResultResponse("SIMPLE_INTEREST", result, DISCLAIMER);
        saveHistory(CalculatorHistory.CalculatorType.SIMPLE_INTEREST, req, result);
        return response;
    }

    // ---------- Compound Interest ----------
    public CalculatorResultResponse calculateCompoundInterest(InterestRequest req) {
        int n = req.getCompoundingPerYear() == null ? 1 : req.getCompoundingPerYear();
        double rate = req.getRatePercent() / 100;
        double maturity = req.getPrincipal() * Math.pow(1 + (rate / n), n * req.getYears());
        double interest = maturity - req.getPrincipal();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("interestEarned", round(interest));
        result.put("maturityAmount", round(maturity));

        CalculatorResultResponse response = new CalculatorResultResponse("COMPOUND_INTEREST", result, DISCLAIMER);
        saveHistory(CalculatorHistory.CalculatorType.COMPOUND_INTEREST, req, result);
        return response;
    }

    // ---------- Investment Returns (SIP / Lumpsum) ----------
    public CalculatorResultResponse calculateInvestmentReturns(InvestmentReturnsRequest req) {
        double monthlyRate = (req.getAnnualReturnPercent() / 12) / 100;
        int months = req.getYears() * 12;
        double futureValue;
        double invested;

        if ("SIP".equalsIgnoreCase(req.getMode())) {
            futureValue = req.getAmount() * (((Math.pow(1 + monthlyRate, months) - 1) / monthlyRate) * (1 + monthlyRate));
            invested = req.getAmount() * months;
        } else { // LUMPSUM
            futureValue = req.getAmount() * Math.pow(1 + (req.getAnnualReturnPercent() / 100), req.getYears());
            invested = req.getAmount();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mode", req.getMode().toUpperCase());
        result.put("totalInvested", round(invested));
        result.put("estimatedReturns", round(futureValue - invested));
        result.put("maturityValue", round(futureValue));

        CalculatorResultResponse response = new CalculatorResultResponse("INVESTMENT_RETURNS", result, DISCLAIMER);
        saveHistory(CalculatorHistory.CalculatorType.INVESTMENT_RETURNS, req, result);
        return response;
    }

    // ---------- History ----------
    public List<Map<String, Object>> getHistoryForCurrentUser() {
        User user = currentUserService.getCurrentUser();
        return calculatorHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(h -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("id", h.getId());
                    entry.put("calculatorType", h.getCalculatorType().name());
                    entry.put("input", readJson(h.getInputJson()));
                    entry.put("result", readJson(h.getResultJson()));
                    entry.put("createdAt", h.getCreatedAt());
                    return entry;
                })
                .collect(Collectors.toList());
    }

    private void saveHistory(CalculatorHistory.CalculatorType type, Object input, Object result) {
        try {
            User user = currentUserService.getCurrentUser();
            CalculatorHistory history = new CalculatorHistory();
            history.setUser(user);
            history.setCalculatorType(type);
            history.setInputJson(objectMapper.writeValueAsString(input));
            history.setResultJson(objectMapper.writeValueAsString(result));
            calculatorHistoryRepository.save(history);
        } catch (Exception ignored) {
            // Calculator still returns a result even if history logging fails or user isn't authenticated (guest use)
        }
    }

    private Object readJson(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return json;
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
