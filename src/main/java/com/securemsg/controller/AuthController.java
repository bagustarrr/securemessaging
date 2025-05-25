package com.securemsg.controller;

import com.securemsg.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Главная страница -> перенаправление на /chat
    @GetMapping("/")
    public String home() {
        return "redirect:/chat";
    }

    // Страница входа (если уже вошел — редирект на чат)
    @GetMapping("/login")
    public String loginPage(Authentication authentication) {
        if (authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/chat";
        }
        return "login";
    }

    // Страница регистрации
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // Обработка формы регистрации
    @PostMapping("/register")
    public String registerUser(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            return "redirect:/register?error=nomatch";
        }
        if (userService.register(name, email, password).isEmpty()) {
            return "redirect:/register?error=exists";
        }
        return "redirect:/login?registered";
    }
}
