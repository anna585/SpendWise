package com.example.spendwise.controller;

import com.example.spendwise.entity.Category;
import com.example.spendwise.entity.Transaction;
import com.example.spendwise.entity.TransactionType;
import com.example.spendwise.form.TransactionForm;
import com.example.spendwise.service.CategoryNotFoundException;
import com.example.spendwise.service.CategoryService;
import com.example.spendwise.service.TransactionCategoryTypeMismatchException;
import com.example.spendwise.service.TransactionService;
import com.example.spendwise.service.TransactionNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final CategoryService categoryService;

    public TransactionController(
            TransactionService transactionService,
            CategoryService categoryService
    ) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("transactions", transactionService.findAll());
        return "transactions/list";
    }

    @GetMapping("/new")
    public String newTransaction(Model model) {
        model.addAttribute("transactionForm", new TransactionForm());
        model.addAttribute("formAction", "/transactions");
        model.addAttribute("pageTitle", "Add transaction");
        addFormOptions(model);
        return "transactions/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("transactionForm") TransactionForm transactionForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addFormOptions(model);
            return "transactions/form";
        }

        try {
            transactionService.create(
                    transactionForm.getType(),
                    transactionForm.getAmount(),
                    transactionForm.getTransactionDate(),
                    transactionForm.getDescription(),
                    transactionForm.getCategoryId()
            );
        } catch (TransactionCategoryTypeMismatchException | CategoryNotFoundException exception) {
            bindingResult.rejectValue("categoryId", "transaction.category.invalid", exception.getMessage());
            addFormOptions(model);
            return "transactions/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Transaction created.");
        return "redirect:/transactions";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Transaction transaction = transactionService.findById(id);
        TransactionForm transactionForm = new TransactionForm();
        transactionForm.setType(transaction.getType());
        transactionForm.setAmount(transaction.getAmount());
        transactionForm.setTransactionDate(transaction.getTransactionDate());
        transactionForm.setDescription(transaction.getDescription());
        transactionForm.setCategoryId(transaction.getCategory().getId());
        model.addAttribute("transactionForm", transactionForm);
        model.addAttribute("transactionId", id);
        model.addAttribute("formAction", "/transactions/" + id);
        model.addAttribute("pageTitle", "Edit transaction");
        addFormOptions(model);
        return "transactions/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("transactionForm") TransactionForm transactionForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addEditFormState(model, id);
            addFormOptions(model);
            return "transactions/form";
        }

        try {
            transactionService.update(
                    id,
                    transactionForm.getType(),
                    transactionForm.getAmount(),
                    transactionForm.getTransactionDate(),
                    transactionForm.getDescription(),
                    transactionForm.getCategoryId()
            );
        } catch (TransactionCategoryTypeMismatchException | CategoryNotFoundException exception) {
            bindingResult.rejectValue("categoryId", "transaction.category.invalid", exception.getMessage());
            addEditFormState(model, id);
            addFormOptions(model);
            return "transactions/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Transaction updated.");
        return "redirect:/transactions";
    }

    @GetMapping("/{id}/delete")
    public String confirmDelete(@PathVariable Long id, Model model) {
        model.addAttribute("transaction", transactionService.findById(id));
        return "transactions/delete";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        transactionService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Transaction deleted.");
        return "redirect:/transactions";
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String transactionNotFound(TransactionNotFoundException exception, Model model) {
        model.addAttribute("errorMessage", exception.getMessage());
        return "transactions/not-found";
    }

    private void addEditFormState(Model model, Long id) {
        model.addAttribute("transactionId", id);
        model.addAttribute("formAction", "/transactions/" + id);
        model.addAttribute("pageTitle", "Edit transaction");
    }

    private void addFormOptions(Model model) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("transactionTypes", TransactionType.values());
    }
}
