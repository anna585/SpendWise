package com.example.spendwise.controller;

import com.example.spendwise.entity.MonthlyBudget;
import com.example.spendwise.entity.TransactionType;
import com.example.spendwise.form.MonthlyBudgetForm;
import com.example.spendwise.service.BudgetReportRow;
import com.example.spendwise.service.CategoryNotFoundException;
import com.example.spendwise.service.DuplicateMonthlyBudgetException;
import com.example.spendwise.service.InvalidBudgetCategoryException;
import com.example.spendwise.service.MonthlyBudgetNotFoundException;
import com.example.spendwise.service.MonthlyBudgetService;
import com.example.spendwise.service.CategoryService;
import jakarta.validation.Valid;
import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/budgets")
public class MonthlyBudgetController {

    private final MonthlyBudgetService budgetService;
    private final CategoryService categoryService;

    public MonthlyBudgetController(
            MonthlyBudgetService budgetService,
            CategoryService categoryService
    ) {
        this.budgetService = budgetService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String month, Model model) {
        YearMonth selectedMonth = parseMonth(month, model);
        List<BudgetReportRow> report = budgetService.getReport(selectedMonth);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("reportRows", report);
        return "budgets/list";
    }

    @GetMapping("/new")
    public String newBudget(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Long categoryId,
            Model model
    ) {
        YearMonth selectedMonth = parseMonth(month, model);
        MonthlyBudgetForm form = new MonthlyBudgetForm();
        form.setMonth(selectedMonth.toString());
        form.setCategoryId(categoryId);
        model.addAttribute("budgetForm", form);
        model.addAttribute("formAction", "/budgets");
        model.addAttribute("pageTitle", "Create monthly budget");
        addFormOptions(model);
        return "budgets/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("budgetForm") MonthlyBudgetForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!bindingResult.hasErrors()) {
            try {
                budgetService.create(
                        YearMonth.parse(form.getMonth()),
                        form.getAmount(),
                        Objects.requireNonNull(form.getCategoryId())
                );
            } catch (DateTimeException exception) {
                bindingResult.rejectValue("month", "budget.month.invalid", "Select a valid month.");
            } catch (DuplicateMonthlyBudgetException exception) {
                bindingResult.rejectValue("categoryId", "budget.duplicate", exception.message());
            } catch (CategoryNotFoundException | InvalidBudgetCategoryException exception) {
                bindingResult.rejectValue(
                        "categoryId",
                        "budget.category.invalid",
                        "Select an existing expense category."
                );
            }
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/budgets");
            model.addAttribute("pageTitle", "Create monthly budget");
            addFormOptions(model);
            return "budgets/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Budget created.");
        return redirectToMonth(form.getMonth());
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable @NonNull Long id, Model model) {
        MonthlyBudget budget = budgetService.findById(id);
        MonthlyBudgetForm form = new MonthlyBudgetForm();
        form.setMonth(YearMonth.from(budget.getMonth()).toString());
        form.setAmount(budget.getAmount());
        form.setCategoryId(budget.getCategory().getId());
        model.addAttribute("budgetForm", form);
        model.addAttribute("budgetId", id);
        model.addAttribute("formAction", "/budgets/" + id);
        model.addAttribute("pageTitle", "Edit monthly budget");
        addFormOptions(model);
        return "budgets/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable @NonNull Long id,
            @Valid @ModelAttribute("budgetForm") MonthlyBudgetForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!bindingResult.hasErrors()) {
            try {
                budgetService.update(
                        id,
                        YearMonth.parse(form.getMonth()),
                        form.getAmount(),
                        Objects.requireNonNull(form.getCategoryId())
                );
            } catch (DateTimeException exception) {
                bindingResult.rejectValue("month", "budget.month.invalid", "Select a valid month.");
            } catch (DuplicateMonthlyBudgetException exception) {
                bindingResult.rejectValue("categoryId", "budget.duplicate", exception.message());
            } catch (CategoryNotFoundException | InvalidBudgetCategoryException exception) {
                bindingResult.rejectValue(
                        "categoryId",
                        "budget.category.invalid",
                        "Select an existing expense category."
                );
            }
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("budgetId", id);
            model.addAttribute("formAction", "/budgets/" + id);
            model.addAttribute("pageTitle", "Edit monthly budget");
            addFormOptions(model);
            return "budgets/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Budget updated.");
        return redirectToMonth(form.getMonth());
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable @NonNull Long id,
            @RequestParam String month,
            RedirectAttributes redirectAttributes
    ) {
        budgetService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Budget deleted.");
        return redirectToMonth(month);
    }

    @ExceptionHandler(MonthlyBudgetNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String budgetNotFound(MonthlyBudgetNotFoundException exception, Model model) {
        model.addAttribute("errorMessage", exception.message());
        return "budgets/not-found";
    }

    private YearMonth parseMonth(String month, Model model) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month);
        } catch (DateTimeException exception) {
            model.addAttribute("monthError", "Select a valid month.");
            return YearMonth.now();
        }
    }

    private String redirectToMonth(String month) {
        return "redirect:/budgets?month=" + YearMonth.parse(month);
    }

    private void addFormOptions(Model model) {
        model.addAttribute("categories", categoryService.findAllByType(TransactionType.EXPENSE));
    }
}
