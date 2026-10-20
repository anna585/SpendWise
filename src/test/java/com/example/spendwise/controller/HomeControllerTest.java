package com.example.spendwise.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.spendwise.service.MonthlySummary;
import com.example.spendwise.service.TransactionService;
import java.math.BigDecimal;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void displaysCurrentMonthSummaryOnHomePage() throws Exception {
        when(transactionService.getMonthlySummary(YearMonth.now()))
                .thenReturn(new MonthlySummary(
                        new BigDecimal("1250.00"),
                        new BigDecimal("475.25"),
                        new BigDecimal("774.75")
                ));

        var result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("monthlySummary"))
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .contains("1250.00", "475.25", "774.75");
        verify(transactionService).getMonthlySummary(YearMonth.now());
    }
}
