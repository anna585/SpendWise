package com.example.spendwise.controller;

import com.example.spendwise.service.TransactionService;
import java.time.YearMonth;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final TransactionService transactionService;

    public HomeController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("monthlySummary", transactionService.getMonthlySummary(YearMonth.now()));
        return "home";
    }
}
