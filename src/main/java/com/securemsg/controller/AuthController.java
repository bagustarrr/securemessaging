package com.securemsg.controller;

import com.securemsg.model.User;
import com.securemsg.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(Model model, Authentication authentication,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "registered", required = false) String registered) {
        // If already logged in, skip login page
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/chat";
        }
        if (error != null) {
            model.addAttribute("errorMsg", "invalid");  // key for "Invalid IIN or password"
        }
        if (logout != null) {
            model.addAttribute("successMsg", "logoutSuccess");  // key for logout success message
        }
        if (registered != null) {
            model.addAttribute("successMsg", "registeredSuccess"); // key for registration success msg
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") @Valid User formUser,
                               BindingResult bindingResult,
                               @RequestParam("confirmPassword") String confirmPassword,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "validationError");  // generic validation error message key
            return "register";
        }
        if (!formUser.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "passwordsNoMatch");  // key for "Passwords do not match"
            return "register";
        }
        try {
            userService.registerUser(formUser);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
        // Registration successful – redirect to login page with flag
        return "redirect:/login?registered";
    }
}
