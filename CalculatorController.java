package com.fintax.controller;

import com.fintax.dto.calculator.*;
import com.fintax.service.CalculatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calculators")
@RequiredArgsConstructor
public class CalculatorController {

    private final CalculatorService calculatorService;

    @PostMapping("/income-tax")
    public ResponseEntity<CalculatorResultResponse> incomeTax(@Valid @RequestBody IncomeTaxRequest request) {
        return ResponseEntity.ok(calculatorService.calculateIncomeTax(request));
    }

    @PostMapping("/emi")
    public ResponseEntity<CalculatorResultResponse> emi(@Valid @RequestBody EmiRequest request) {
        return ResponseEntity.ok(calculatorService.calculateEmi(request));
    }

    @PostMapping("/simple-interest")
    public ResponseEntity<CalculatorResultResponse> simpleInterest(@Valid @RequestBody InterestRequest request) {
        return ResponseEntity.ok(calculatorService.calculateSimpleInterest(request));
    }

    @PostMapping("/compound-interest")
    public ResponseEntity<CalculatorResultResponse> compoundInterest(@Valid @RequestBody InterestRequest request) {
        return ResponseEntity.ok(calculatorService.calculateCompoundInterest(request));
    }

    @PostMapping("/investment-returns")
    public ResponseEntity<CalculatorResultResponse> investmentReturns(@Valid @RequestBody InvestmentReturnsRequest request) {
        return ResponseEntity.ok(calculatorService.calculateInvestmentReturns(request));
    }

    // Requires auth — only meaningful for logged-in users
    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> history() {
        return ResponseEntity.ok(calculatorService.getHistoryForCurrentUser());
    }
}
