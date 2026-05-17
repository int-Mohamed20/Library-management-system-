package com.library.controller;

import com.library.model.User;
import com.library.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        if (user.getRole() == User.Role.ADMIN) return "redirect:/admin/dashboard";
        return "redirect:/student/home";
    }

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, Model model) {
        String savedEmail = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("rememberedEmail")) {
                    savedEmail = c.getValue();
                }
            }
        }
        model.addAttribute("savedEmail", savedEmail);
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        @RequestParam(defaultValue = "false") boolean rememberMe,
                        HttpSession session,
                        HttpServletResponse response,
                        Model model) {

        Optional<User> optUser = userService.login(email, password);
        if (optUser.isEmpty()) {
            model.addAttribute("error", "Invalid email or password");
            return "auth/login";
        }

        User user = optUser.get();
        session.setAttribute("user", user);

        if (rememberMe) {
            Cookie cookie = new Cookie("rememberedEmail", email);
            cookie.setMaxAge(7 * 24 * 60 * 60);
            cookie.setPath("/");
            response.addCookie(cookie);
        } else {
            Cookie cookie = new Cookie("rememberedEmail", "");
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        if (user.getRole() == User.Role.ADMIN) return "redirect:/admin/dashboard";
        return "redirect:/student/home";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           BindingResult result,
                           Model model) {
        if (result.hasErrors()) return "auth/register";

        try {
            userService.register(user);
            return "redirect:/login?registered=true";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();
        Cookie cookie = new Cookie("rememberedEmail", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/login";
    }
}
