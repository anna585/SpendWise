package com.example.spendwise.controller;

import com.example.spendwise.entity.TransactionType;
import com.example.spendwise.form.CategoryForm;
import com.example.spendwise.service.CategoryAlreadyExistsException;
import com.example.spendwise.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "categories/list";
    }

    @GetMapping("/new")
    public String newCategory(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        addTransactionTypes(model);
        return "categories/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("categoryForm") CategoryForm categoryForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addTransactionTypes(model);
            return "categories/form";
        }

        try {
            categoryService.create(
                    categoryForm.getName(),
                    categoryForm.getType(),
                    categoryForm.getDescription()
            );
        } catch (CategoryAlreadyExistsException exception) {
            bindingResult.rejectValue("name", "category.name.duplicate", exception.getMessage());
            addTransactionTypes(model);
            return "categories/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Category created.");
        return "redirect:/categories";
    }

    private void addTransactionTypes(Model model) {
        model.addAttribute("transactionTypes", TransactionType.values());
    }
}
