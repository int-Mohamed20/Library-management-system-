package com.library.controller;

import com.library.model.BorrowRecord;
import com.library.model.User;
import com.library.service.BookService;
import com.library.service.BorrowService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final BookService bookService;
    private final BorrowService borrowService;

    public StudentController(BookService bookService, BorrowService borrowService) {
        this.bookService = bookService;
        this.borrowService = borrowService;
    }

    private User getUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        model.addAttribute("books", bookService.getAvailableBooks());
        return "student/home";
    }

    @GetMapping("/books")
    public String books(@RequestParam(required = false) String search,
                        HttpSession session, Model model) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";

        List books = (search != null && !search.isBlank())
                ? bookService.search(search)
                : bookService.getAllBooks();

        model.addAttribute("books", books);
        model.addAttribute("search", search);
        return "student/books";
    }

    @PostMapping("/borrow/{bookId}")
    public String borrow(@PathVariable Long bookId, HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";

        String result = borrowService.borrowBook(user, bookId);
        if (result.equals("success")) {
            redirectAttributes.addFlashAttribute("success", "Book borrowed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }
        return "redirect:/student/books";
    }

    @GetMapping("/history")
    public String history(HttpSession session, Model model) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("records", borrowService.getUserHistory(user));
        model.addAttribute("user", user);
        return "student/history";
    }

    @PostMapping("/return/{recordId}")
    public String returnBook(@PathVariable Long recordId, HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";

        String result = borrowService.returnBook(recordId, user);
        if (result.equals("success")) {
            redirectAttributes.addFlashAttribute("success", "Book returned successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }
        return "redirect:/student/history";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = getUser(session);
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        List<BorrowRecord> records = borrowService.getUserHistory(user);
        long active = records.stream().filter(r -> r.getStatus() == BorrowRecord.Status.BORROWED).count();
        model.addAttribute("totalBorrowed", records.size());
        model.addAttribute("activeBorrows", active);
        return "student/profile";
    }
}
