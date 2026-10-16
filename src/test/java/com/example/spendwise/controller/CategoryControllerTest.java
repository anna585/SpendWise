package com.example.spendwise.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.spendwise.entity.Category;
import com.example.spendwise.entity.TransactionType;
import com.example.spendwise.service.CategoryAlreadyExistsException;
import com.example.spendwise.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void listsCategories() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of(
                new Category("Dining", TransactionType.EXPENSE)
        ));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/list"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    void rendersCategoryFormWithAvailableTypes() throws Exception {
        mockMvc.perform(get("/categories/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/form"))
                .andExpect(model().attributeExists("categoryForm", "transactionTypes"));
    }

    @Test
    void createsCategoryAndRedirects() throws Exception {
        when(categoryService.create("Dining", TransactionType.EXPENSE, "Restaurants"))
                .thenReturn(new Category("Dining", TransactionType.EXPENSE));

        mockMvc.perform(post("/categories")
                        .param("name", "Dining")
                        .param("type", "EXPENSE")
                        .param("description", "Restaurants"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        verify(categoryService).create("Dining", TransactionType.EXPENSE, "Restaurants");
    }

    @Test
    void returnsValidationErrorsWithoutCallingService() throws Exception {
        mockMvc.perform(post("/categories")
                        .param("name", " ")
                        .param("description", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/form"))
                .andExpect(model().attributeHasFieldErrors("categoryForm", "name", "type"));
    }

    @Test
    void reportsDuplicateCategoryNameInForm() throws Exception {
        doThrow(new CategoryAlreadyExistsException("Dining"))
                .when(categoryService)
                .create("Dining", TransactionType.EXPENSE, "");

        mockMvc.perform(post("/categories")
                        .param("name", "Dining")
                        .param("type", "EXPENSE")
                        .param("description", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/form"))
                .andExpect(model().attributeHasFieldErrors("categoryForm", "name"));
    }
}
