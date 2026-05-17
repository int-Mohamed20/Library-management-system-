package com.library.controller;

import com.library.model.Book;
import com.library.model.User;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookService bookService;
    private final UserService userService;
    private final BorrowService borrowService;

    public AdminController(BookService bookService, UserService userService, BorrowService borrowService) {
        this.bookService = bookService;
        this.userService = userService;
        this.borrowService = borrowService;
    }

    private User getAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.Role.ADMIN) return null;
        return user;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (getAdmin(session) == null) return "redirect:/login";
        model.addAttribute("totalBooks", bookService.getAllBooks().size());
        model.addAttribute("totalStudents", userService.getAllStudents().size());
        model.addAttribute("activeLoans", borrowService.getActiveBorrows().size());
        model.addAttribute("recentRecords", borrowService.getAllRecords().stream().limit(5).toList());
        return "admin/dashboard";
    }

    @GetMapping("/books")
    public String books(HttpSession session, Model model) {
        if (getAdmin(session) == null) return "redirect:/login";
        model.addAttribute("books", bookService.getAllBooks());
        model.addAttribute("newBook", new Book());
        return "admin/books";
    }

    @PostMapping("/books/add")
    public String addBook(@Valid @ModelAttribute("newBook") Book book,
                          BindingResult result, HttpSession session,
                          Model model, RedirectAttributes redirectAttributes) {
        if (getAdmin(session) == null) return "redirect:/login";
        if (result.hasErrors()) {
            model.addAttribute("books", bookService.getAllBooks());
            return "admin/books";
        }
        book.setAvailableCopies(book.getTotalCopies());
        bookService.save(book);
        redirectAttributes.addFlashAttribute("success", "Book added successfully!");
        return "redirect:/admin/books";
    }

    @GetMapping("/books/edit/{id}")
    public String editBookForm(@PathVariable Long id, HttpSession session, Model model) {
        if (getAdmin(session) == null) return "redirect:/login";
        bookService.findById(id).ifPresent(b -> model.addAttribute("book", b));
        return "admin/edit-book";
    }

    @PostMapping("/books/edit/{id}")
    public String editBook(@PathVariable Long id, @Valid @ModelAttribute("book") Book book,
                           BindingResult result, HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (getAdmin(session) == null) return "redirect:/login";
        if (result.hasErrors()) return "admin/edit-book";
        book.setId(id);
        bookService.save(book);
        redirectAttributes.addFlashAttribute("success", "Book updated!");
        return "redirect:/admin/books";
    }

    @PostMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id, HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (getAdmin(session) == null) return "redirect:/login";
        bookService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Book deleted.");
        return "redirect:/admin/books";
    }

    @GetMapping("/students")
    public String students(HttpSession session, Model model) {
        if (getAdmin(session) == null) return "redirect:/login";
        model.addAttribute("students", userService.getAllStudents());
        return "admin/students";
    }

    @PostMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (getAdmin(session) == null) return "redirect:/login";
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("success", "Student removed.");
        return "redirect:/admin/students";
    }

    @GetMapping("/loans")
    public String loans(HttpSession session, Model model) {
        if (getAdmin(session) == null) return "redirect:/login";
        model.addAttribute("records", borrowService.getAllRecords());
        return "admin/loans";
    }
}
